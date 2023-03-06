# Implementing Refactorings

This document describes how to implement refactorings (source code manipulation without diagnostics).

It will build up on the concepts of [implementing quickfixes](/docs/implementing-quickfixes.md) as the APIs are similar.
If anything is too confusing or not explained in depth, have a look at the documentation about quickfixes.

We will look at `COMPRESS` statements and how to add `FULL` to them.

So the following code:

```
COMPRESS #NUM-VAR INTO #TEXT
```

will offer a refactoring to transform it into

```
COMPRESS FULL #NUM-VAR INTO #TEXT
```

## Defining the code action


We can create a new refactoring in the `natls` module within the package `org.amshove.natls.refactorings`.
The name of our new class will be `CompressRefactorings`.

This class will implement `ICodeActionProvider`:

```java
public class CompressRefactorings implements ICodeActionProvider
{
    @Override
    public boolean isApplicable(RefactoringContext context)
    {
        return false;
    }

    @Override
    public List<CodeAction> createCodeAction(RefactoringContext context)
    {
        return null;
    }
}
```

The method `isApplicable()` should return whether the refactoring should be shown to the user or not.

`createCodeAction()` will return the actual `CodeAction`s to manipulate the source code.

Since we want to refactor `COMPRESS`-statements, our `isApplicable()` method should return true if the cursor is on a `ICompressStatementNode` and the statement doesn't already have `FULL`.
We can do so by using `RefactoringContext::nodeAtPosition()`:

````java
@Override
public boolean isApplicable(RefactoringContext context)
{
    return context.nodeAtPosition() instanceof ICompressStatementNode compressStatementNode && !compressStatementNode.isFull();
}
````

That's all we need there.

Within the `createCodeAction()` we want to return a `CodeAction` as we did with the quickfix for `COMPRESS NUMERIC`. The difference is, that we change `COMPRESS` to `COMPRESS FULL`.

```java
@Override
public List<CodeAction> createCodeAction(RefactoringContext context)
{
    var compressKeyword = context.nodeAtPosition().findDescendantToken(SyntaxKind.COMPRESS);

    if (compressKeyword == null)
    {
        return List.of();
    }

    return List.of(
        new CodeActionBuilder("Add FULL to COMPRESS", CodeActionKind.RefactorInline)
            .appliesWorkspaceEdit(new WorkspaceEditBuilder()
            .changesText(compressKeyword.position(), "COMPRESS FULL"))
        .build()
    );
}
```

## Testing the refactoring

We will create a new class `CompressRefactoringsShould` in the test sources in the package `org.amshove.natls.refactorings`.

We add three test cases:

- Test that the refactoring is offered when the cursor is on a `COMPRESS` without full
- Test that the refactoring is **not** offered when the `COMPRESS` already has `FULL`
- Test that the applied refactoring actually adds `FULL`

```java

class CompressRefactoringsShould extends CodeActionTest
{
    private static LspTestContext testContext;

    @Test
    void beApplicableWhenHoveringACompressWithoutFull()
    {
        assertSingleCodeAction(
                "Add FULL to COMPRESS",
                "LIBONE",
                "SUBN.NSN",
                """
                    DEFINE DATA
                    LOCAL
                    1 #VAR (A10)
                    1 #TEXT (A) DYNAMIC
                    END-DEFINE
                    COM${}$PRESS #VAR INTO #TEXT
                    END
                """
        );
    }

    @Test
    void notOfferToAddFullWhenFullIsAlreadyPresent()
    {
        assertNoCodeAction(
                "LIBONE",
                "SUBN.NSN",
                """ 
                    DEFINE DATA
                    LOCAL
                    1 #VAR (A10)
                    1 #TEXT (A) DYNAMIC
                    END-DEFINE
                    COM${}$PRESS FULL #VAR INTO #TEXT
                    END
                """,
                "Add FULL to COMPRESS"
        );
    }

    @Test
    void addFullToCompressIfApplied()
    {
        assertSingleCodeAction(
                "Add FULL to COMPRESS",
                "LIBONE",
                "SUBN.NSN",
                """
                    DEFINE DATA
                    LOCAL
                    1 #VAR (A10)
                    1 #TEXT (A) DYNAMIC
                    END-DEFINE
                    COM${}$PRESS #VAR INTO #TEXT
                    END
                """
        )
        .resultsApplied(
                """
                    DEFINE DATA
                    LOCAL
                    1 #VAR (A10)
                    1 #TEXT (A) DYNAMIC
                    END-DEFINE
                    COMPRESS #VAR INTO #TEXT
                    END
                """,
                """
                    DEFINE DATA
                    LOCAL
                    1 #VAR (A10)
                    1 #TEXT (A) DYNAMIC
                    END-DEFINE
                    COMPRESS FULL #VAR INTO #TEXT
                    END
                """
        );
    }

    @BeforeAll
    static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
    {
        testContext = context;
    }

    @Override
    protected LspTestContext getContext()
    {
        return testContext;
    }

    @Override
    protected ICodeActionProvider getCodeActionUnderTest()
    {
        return new CompressRefactorings();
    }

}
```
