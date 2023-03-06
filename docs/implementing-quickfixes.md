# Implementing Quickfixes

We can implement quickfixes to offer the user an automated way to solve diagnostics.

In this document we will use the diagnostic of the analyzer we've built in the [implementing analyzers](/docs/implementing-analyzers.md) documentation.

## Implementing a quickfix

To implement a quickfix, we have to create a new class in the module `natls` in the package `org.amshove.natls.quickfixes` which will extend `AbstractQuickFix`.

In our case, we'll create the following class:

```java
public class CompressNumericQuickFix extends AbstractQuickFix
{
    @Override
    protected void registerQuickfixes()
    {

    }
}
```

Within the `registerQuickfixes()` method we call `registerQuickfix()` passing either a `DiagnosticDescription`, or an id of the diagnostic we want to fix, followed by a method reference to a method which will create the quickfix.

Doing so will result in the following structure:

```java
public class CompressNumericQuickFix extends AbstractQuickFix
{
    @Override
    protected void registerQuickfixes()
    {
        registerQuickFix(CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC, this::addNumericToCompress);
    }

    private CodeAction addNumericToCompress(QuickFixContext quickFixContext)
    {
    }
}
```

The `addNumericToCompress()` method has to return a `CodeAction`. A `CodeAction` describes the source code modification that will be done by the language server client.

We can use the `CodeActionBuilder` class to use some convenience methods while creating a code action.

The constructor of the builder takes a title (which will be shown to the user) and a kind, which we can take from the class `CodeActionKind`.

We will start by passing those two things:

```java
return new CodeActionBuilder("Add NUMERIC to COMPRESS", CodeActionKind.QuickFix)
```

The next thing we do is to let the quickfix know which diagnostic it fixes:

```java
return new CodeActionBuilder("Add NUMERIC to COMPRESS", CodeActionKind.QuickFix)
    .fixesDiagnostic(quickFixContext.diagnostic())
```

The actual change in the code will be registered with `appliesWorkspaceEdit()`, which itself takes a `WorkspaceEditBuilder`.
The `WorkspaceEditBuilder` has a few methods to manipulate the source file, e.g. `addsUsing()`, `addsVariable()`, `changesText()`, etc.

Let's look at the compress statement that we want to change and what it should become:

```
COMPRESS #VAR INTO #TEXT /* before
COMPRESS NUMERIC #VAR INTO #TEXT /* after
```

What we can do in this case is replacing (e.g. `changesText()`) the `COMPRESS` with `COMPRESS NUMERIC`.
To accomplish this, we have to get the position of the `COMPRESS` keyword.

We know that the diagnostic was raised for the `ICompressStatementNode` and that it will have the keyword `COMPRESS`.
We can ask the `QuickFixContext` to give us the node that raised the diagnostic with `nodeAtPosition()`.
Every `ISyntaxNode` is itself considered a tree. This means we can ask the node to give us the first `ITokenNode` which is of kind `COMPRESS` (I'll add this before the `return`):

````java
var compressKeyword = quickFixContext.nodeAtPosition().findDescendantToken(SyntaxKind.COMPRESS);
````

Since `findDescendantToken()` might return null, we add a check to guard against it and return null ourselves:

```java
var compressKeyword = quickFixContext.nodeAtPosition().findDescendantToken(SyntaxKind.COMPRESS);

if (compressKeyword == null)
{
    return null;
}
```

The keyword token will automatically give us the position that we want to change, so we can complete our implementation:

```java
public class CompressNumericQuickFix extends AbstractQuickFix
{
    @Override
    protected void registerQuickfixes()
    {
        registerQuickFix(CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC, this::addNumericToCompress);
    }

    private CodeAction addNumericToCompress(QuickFixContext quickFixContext)
    {
        var compressKeyword = quickFixContext.nodeAtPosition().findDescendantToken(SyntaxKind.COMPRESS);

        if (compressKeyword == null)
        {
            return null;
        }

        return new CodeActionBuilder("Add NUMERIC to COMPRESS", CodeActionKind.QuickFix)
                .fixesDiagnostic(quickFixContext.diagnostic())
                .appliesWorkspaceEdit(new WorkspaceEditBuilder()
                        .changesText(compressKeyword.position(), "COMPRESS NUMERIC")
                )
                .build();
    }
}
```

## Testing the implementation

As we did with our analyzer, we also want to write tests for our quick fix to make sure it is working and continues to do so.

We create a new class in the test source folder: `org.amshove.natls.quickfixes.CompressNumericQuickFixShould`.

This class has to extend `CodeActionTest`.

Tests for code actions are a bit more involved than tests for analyzers because we have to build a complete context with a valid natural project that the language server can initialize with.

To do so we have to return a `LspTestContext` in `getContext()` and initialize the code action in `getCodeActionUnderTest()`.

We can initialize a project from the `resources` folder which contains a `emptyproject` folder with the `.natural` file set up.
Doing this statically with `@BeforeAll` makes sure that the project is initialized once for all the tests in this class.

The class looks like this:

```java
class CompressNumericQuickFixShould extends CodeActionTest
{
    private static LspTestContext testContext;

    @BeforeAll
    static void setupProject(@LspProjectName("emptyproject") LspTestContext context)
    {
        testContext = context;
    }

    @Override
    protected ICodeActionProvider getCodeActionUnderTest()
    {
        return new CompressNumericQuickFix();
    }

    @Override
    protected LspTestContext getContext()
    {
        return testContext;
    }
}
```

We can now create our first test (I will add them atop of the field to hide all the ceremony):

```java
@Test
void addsNumericToACompressWhichUsesFloatingNumberTypes()
{
    var result = receiveCodeActions("LIBONE", "SUBMOD.NSN", """
        DEFINE DATA
        LOCAL
        1 #VAR (N12,7)
        1 #TEXT (A) DYNAMIC
        END-DEFINE
        COM${}$PRESS #VAR INTO #TEXT /* Notice the cursor
        END
        """); // 1

    var actions = result.codeActions(); // 2

    assertContainsCodeAction("Add NUMERIC to COMPRESS", actions); // 3

    assertSingleCodeAction(actions) // 4
        .resultsApplied(result.savedSource(), """
        DEFINE DATA
        LOCAL
        1 #VAR (N12,7)
        1 #TEXT (A) DYNAMIC
        END-DEFINE
        COMPRESS NUMERIC #VAR INTO #TEXT
        END
        """); // 5
}
```

These tests are a bit more involved than the previous ones for analyzers, so we take a step back and look at each line.

1. Create a new file with source code

The `receiveCodeActions` method creates a new file in the empty project in the library `LIBONE` and calls it `SUBMOD.NSN`.
Within the `COMPRESS` keyword you can find the following: `${}$`.
This denotes where the cursor of the user is sitting when asking for code actions.

You can think about this as a selection within the editor. `${}$` means an empty selection, e.g. just the cursor. `${COMPRESS}$` means that the word `COMPRESS` is selected.

2. We receive the `CodeAction`s that the language server is offering us for the given file and the given cursor position.

3. We make sure that the received code actions contain a action with the title `Add NUMERIC to COMPRESS`.
4. We assert that we only have a single code action.
5. We assert on the text modification that will happen when the quickfix is applied.

`result.savedSource()` returns the source code with the quickfix applied. We pass this and the source that we expect to `resultsApplied()`.
This will make sure that the actual edit of the code matches what we expect.

We're done! We've implemented the quickfix and a test for the quickfix.

If you want to move on, have a look at [implementing refactorings](/docs/implementing-refactorings.md) where we will do something similar without having a diagnostic.
