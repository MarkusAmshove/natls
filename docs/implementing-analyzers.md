# Implementing Analyzers

NatLS has a plugin-like interface to add new analyzers.

This guide demonstrates the steps to write an analyzer which raises a diagnostic
on `COMPRESS` statements which compress variables that have a floating point type
when no `NUMERIC` is present.

The following program should raise a diagnostic:

```
DEFINE DATA
LOCAL
1 #VAR (N12,4)
1 #TEXT (A) DYNAMIC
END-DEFINE

COMPRESS #VAR INTO #TEXT /* no NUMERIC present

END
```

while this should not:

```
DEFINE DATA
LOCAL
1 #VAR (N12,4)
1 #TEXT (A) DYNAMIC
END-DEFINE

COMPRESS NUMERIC #VAR INTO #TEXT /* NUMERIC present

END
```

## Use `explore` to find what has to be analyzed

This repository contains a tool called `explore` which is used as a parser explorer program.

To know which part of the AST we have to analyze, we can use the tool to find the type of the node we're interested in.

To run explore simply run `./gradlew :explore:run`.

You will be greeted by the explore window:

![explore window](/assets/implementing_analyzers/explore_start.png)

In the editor window we can type our statement and press `CTRL+Return` to parse the source:

![parsed statement](/assets/implementing_analyzers/parsed_statement.png)

On the right side of the editor you can see a tree view with the AST containing all nodes.
On the bottom you can see all the tokens that the lexer spit out.

Pressing on `COMPRESS` in the editor will mark the AST node in the right view and align the tokens on the bottom to the selected line.

![compress pressed](/assets/implementing_analyzers/compress_pressed.png)

The selected AST node is a `TokenNode`, which is one of the few leaf types that exist and represents the keyword `COMPRESS`.
Since we're interested in the statement node, we can click on the `P` button (or press `ALT+Left`) to navigate the AST view to the parent of the current node.

This will select the `CompressStatementNode` and also change the selection in the editor. Notice how the selection in the source editor changes to include all the source of the new selected node.

![compress parent](/assets/implementing_analyzers/compress_parent.png)

As this node is a statement node and all the source we're interested in is selected, we now know that the type we're looking for is `CompressStatementNode` and can move on to implement the analyzer.

## Creating an analyzer

As we found out that the node type we're looking for is a `CompressStatementNode`, with `explore` from the previous step, we can create a new analyzer.

To do so, we create a new class in the module `natlint` in the package `org.amshove.natlint.analyzers`.

We name it `CompressNumericAnalyzer`. The full path relative to the repository root is `libs/natlint/src/main/java/org/amshove/natlint/analyzers/CompressNumericAnalyzer.java`.

To implement an analyzer, we let the new class extend `AbstractAnalyzer` and let the IDE generate the class stub for us, resulting in:

```java
public class CompressNumericAnalyzer extends AbstractAnalyzer
{
    @Override
    public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
    {
        return null;
    }

    @Override
    public void initialize(ILinterContext context)
    {

    }
}
```

### Adding a diagnostic description

The first method that we have to fill returns a list of all the `DiagnosticDescription`s that this analyzer might produce.
This analyzer will only produce one type of diagnostic, so we create a static field with the `DiagnosticDescription` that we can reuse in tests:

```java
public static final DiagnosticDescription COMPRESS_SHOULD_HAVE_NUMERIC = DiagnosticDescription.create(
    "NL100",
    "This COMPRESS statement uses floating point numbers. Did you forget to add NUMERIC?",
    DiagnosticSeverity.INFO
);
```

We can use the `DiagnosticDescription::create` factory method to create a description.

The first parameter we pass is the ID of the diagnostic. This needs to be unique. Conventionally the first two letters are the origin of the diagnostic, `NL` for natlint in this case.
Following the origin comes a number that is incremented.
We can use a high number, like 100 or 1000, for now and adjust it later.

Next we have to return the diagnostic description in `getDiagnosticDescriptions()`:

```java
@Override
public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
{
    return ReadOnlyList.of(COMPRESS_SHOULD_HAVE_NUMERIC);
}
```

Running all tests (or just the `AnalyzerAcceptanceTest`) will now yell at us, because diagnostic ids should not contain a gap:

![acceptance yell](/assets/implementing_analyzers/acceptance_yell.png)

The output of the test tells us that we introduced a gap in diagnostic ids. This also tells us that our id should be `NL012`, so we change the description:

```diff
  public static final DiagnosticDescription COMPRESS_SHOULD_HAVE_NUMERIC = DiagnosticDescription.create(
-      "NL100",
+      "NL012",
      "This COMPRESS statement uses floating point numbers. Did you forget to add NUMERIC?",
      DiagnosticSeverity.INFO
  );
```

### Implementing the analysis

The next step is to register the nodes we're interested in to analyze within the `initialize` method.

As we found with [explore](#use-explore-to-find-what-has-to-be-analyzed) that we want to analyze `CompressStatementNode`s we call `context::registerNodeAnalyzer`:

```java
@Override
public void initialize(ILinterContext context)
{
    context.registerNodeAnalyzer(ICompressStatementNode.class, this::analyzeCompress);
}
```

We pass the type we're interested in as the first parameter. Note that we don't have the actual implementations of the nodes in the scope, so we use the interface `ICompressStatementNode`.
The second parameter we pass is a method reference to the method which will be called for every `COMPRESS` statement that is encountered. This method doesn't exist yet.

If we let our IDE generate the `analyzeCompress` method, we get the following (parameter names might be different depending on your IDE):

```java
private void analyzeCompress(ISyntaxNode node, IAnalyzeContext context)
{
}
```

This function is called for every `COMPRESS` node that is encountered in the module that is analyzed.

We can use this assumption to safely cast `ISyntaxNode` to `ICompressStatementNode`.

```java
private void analyzeCompress(ISyntaxNode node, IAnalyzeContext context)
{
    var compress = (ICompressStatementNode) node;
}
```

Since we want to add `NUMERIC`, we can do an early exit if the `COMPRESS` already specifies `NUMERIC`:

```java
if (compress.isNumeric())
{
    return;
}
```

Looking at how `ICompressStatementNode` is defined, we can see that there is a method called `operands()` which represents all the operands that are compressed into the target.

We will now iterate over all those operands and check if they're a reference to a variable:

```java
private void analyzeCompress(ISyntaxNode node,IAnalyzeContext context)
{
    var compress=(ICompressStatementNode)node;
	
    if (compress.isNumeric())
    {
        return;
    }
	
    for(var operand:compress.operands())
    {
        if(operand instanceof IVariableReferenceNode reference)
        {
            // TODO: Check if floating point type			
        }
    }
}
```

Inside the loop we can use the `IVariableReferenceNode::reference()` method to check if the referenced symbol is a variable with type information (opposed to a group which has no type information):

```java
if (reference.reference() instanceof ITypedVariableNode typedVariable)
{
    // TODO: Check type
}
```

On the `ITypedVariableNode` we use the `type()` method which returns a `IVariableType` containing the actual information about the type.
The `IVariableType` has a method `length()` which contains the length of the variable as it is defined in the source.
This means, that if we encounter `(N7,2)` this will return `7.2`.

Using the modulo operator we can check if the `double` has a decimal part:

```java
if (typedVariable.type().length() % 1 != 0)
{
    // TODO: Raise diagnostic and stop
}
```

To raise a diagnostic, we use the `IAnalyzeContext` parameter of our analysis method and call `IAnalyzeContext::report` using our diagnostic description.

To create a diagnostic we can use the factory method `createDiagnostic()` from our `DiagnosticDescription`.
We have to pass positional information on where the diagnostic should be raised. This can either be a `IPosition` or a `ISyntaxNode`. In this case we can simply pass the compress statement node:

```java
if (typedVariable.type().length() % 1 != 0)
{
    context.report(COMPRESS_SHOULD_HAVE_NUMERIC.createDiagnostic(compress));
	return; // We can stop analyzing this COMPRESS, because we don't need to check the other operands
}
```

The analyzer is now complete.
Here is the full source:

```java
public class CompressNumericAnalyzer extends AbstractAnalyzer
{
    public static final DiagnosticDescription COMPRESS_SHOULD_HAVE_NUMERIC = DiagnosticDescription.create(
            "NL100",
            "This COMPRESS statement uses floating point numbers. Did you forget to add NUMERIC?",
            DiagnosticSeverity.INFO
    );

    @Override
    public ReadOnlyList<DiagnosticDescription> getDiagnosticDescriptions()
    {
        return ReadOnlyList.of(COMPRESS_SHOULD_HAVE_NUMERIC);
    }

    @Override
    public void initialize(ILinterContext context)
    {
        context.registerNodeAnalyzer(ICompressStatementNode.class, this::analyzeCompress);
    }

    private void analyzeCompress(ISyntaxNode node, IAnalyzeContext context)
    {
        var compress = (ICompressStatementNode) node;

        if (compress.isNumeric())
        {
            return;
        }

        for (var operand : compress.operands())
        {
            if (operand instanceof IVariableReferenceNode reference)
            {
                if (reference.reference() instanceof ITypedVariableNode typedVariable)
                {
                    if (typedVariable.type().length() % 1 != 0)
                    {
                        context.report(COMPRESS_SHOULD_HAVE_NUMERIC.createDiagnostic(compress));
                        return;
                    }
                }
            }
        }
    }
}
```

### Testing analyzers

To test our analyzer, we write a test case in `libs/natlint/src/test/java/org/amshove/natlint/analyzers`.
To do so we create a new class in the package called `CompressNumericAnalyzerShould` and extend `AbstractAnalyzerTest`. 

The IDE will yell at us and force us to create a constructor that calls the super class's constructor passing an instance of the analyzer.
We can satisfy this by simply calling the `super` constructor with `new`ing an instance:

```java
class CompressNumericAnalyzerShould extends AbstractAnalyzerTest
{
    protected CompressNumericAnalyzerShould()
    {
        super(new CompressNumericAnalyzer());
    }
}
```

As our first test case, we will create a test that expects a diagnostic to be raised. We can use our introductory example and test it with the `testDiagnostics()` method:

```java
@Test
void raiseADiagnosticIfAnOperandHasAFloatingNumberAndNoNumericIsPresent()
{
    testDiagnostics("""
        DEFINE DATA
        LOCAL
        1 #VAR (N12,4)
        1 #TEXT (A) DYNAMIC
        END-DEFINE

        COMPRESS #VAR INTO #TEXT /* no NUMERIC present

        END
        """,
        expectDiagnostic(6, CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC)
    );
}
```

As the first parameter to `testDiagnostics` we pass the source we want to analyze.
For the second (and possible subsequent) parameter we call `expectDiagnostic` which takes the line number we expect the diagnostic to be and the `DiagnosticDescription` describing the diagnostic that should be raised.
For the latter we can reuse our static field from the analyzer.

Running this test should pass, as the diagnostic is raised.
Give it a try and change the expected line from 6 to 5 to see that the test fails.

We now have a test case that will make sure, that we recognize our case.

Let's make sure that we don't raise a diagnostic when `NUMERIC` is already present:

```java
@Test
void raiseNoDiagnosticIfAnOperandHasAFloatingNumberButNumericIsPresent()
{
    testDiagnostics("""
        DEFINE DATA
        LOCAL
        1 #VAR (N12,4)
        1 #TEXT (A) DYNAMIC
        END-DEFINE

        COMPRESS NUMERIC #VAR INTO #TEXT /* NUMERIC present

        END
        """,
        expectNoDiagnosticOfType(CompressNumericAnalyzer.COMPRESS_SHOULD_HAVE_NUMERIC)
    );
}
```

This test should also pass.
Note that we can either use `expectNoDiagnostic()`, which takes a line number and a description, to make sure that no such diagnostic
is present in that line, or use `expectNoDiagnosticOfType()`, which just takes a description, to make sure that no such diagnostic is present anywhere in the given source.

We're now done with implementing tests for our analyzer.

Please note that this specific analyzer is not completely done yet, as we're for example missing data types like `(F8)`.
This documentation focuses on the APIs that are used to implement analyzers. If you're interested in the full implementation, have a look at the analyzer as it is defined in the repository.

### Adding a diagnostic mapping for SonarQube

If you're this far and ran all the tests (`./gradlew check`) you can see that there is a new test failing: `DiagnosticMappingAcceptanceTests.everyDiagnosticShouldHaveAMapping()`.

This is a test to make sure that every diagnostic that can be raised by the linter or parser has a description for `natqube`.

To create such a description, you have to create a file named after the diagnostic id in `tools/ruletranslator/src/main/resources/rules` relative to the repository root.

We create a file called `NL012` with the following content:

```
name: COMPRESS statement might be missing NUMERIC
priority: MINOR
tags: pitfall
type: BUG
description:
COMPRESS statements compressing floating point numbers need to have the ``NUMERIC`` option to include ``-`` and decimals.

Consider changing it to ``COMPRESS NUMERIC``.
```

Properties:

- `name`: The name of the diagnostic shown in SonarQube
- `priorty`: The priority of the raised issue, either `BLOCKER`, `CRITICAL`, `MAJOR` or `MINOR`
- `tags`: Comma separated list of tags of the issue. [Possible values](https://docs.sonarqube.org/latest/user-guide/built-in-rule-tags/)
- `type`: Type of the issue, either `CODE_SMELL`, `BUG`, `VULNERABILITY` or `SECURITY_HOTSPOT`
- `description`: A description of the issue. This should be human-readable and explain what the issue is and how to resolve it.

Note that for SonarQube to format stuff as code (e.g. in mono font) you have to use double backticks, opposed to single backticks in markdown. You can find all formatting related information in the [SonarQube documentation](https://next.sonarqube.com/sonarqube/formatting/help).

Now all the tests are happy, and we are done implementing our analyzer.

### Next steps

As we've implemented our analyzer, we can now consider implementing a quickfix to offer an automated fix of the issue.
Follow the [implementing quickfixes](/docs/implementing-quickfixes.md) documentation to do so. That documentation will build up on the analyzer we implemented in this document.
