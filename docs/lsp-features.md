All these examples are recorded using Visual Studio Code as a client for the language server.
As everything is implemented using endpoints from the Language Server Protocol (LSP) these features are available to all editors that support the needed API.

# Analyzing as you type

![analyzing_as_you_type](/assets/lsp_features/analyzing_as_you_type.gif)

Diagnostics get updated on the fly as you type

# Hover documentation

![variable_hover](/assets/lsp_features/variable_hover.png) ![module_hover](/assets/lsp_features/module_hover.png)

Hovering over variables and modules takes documentation that is written as comments into account

Hovering is also implemented for system functions and variables:

![systemfunction_hover](/assets/lsp_features/systemfunction_hover.png)

# Code completion

![code_completion](/assets/lsp_features/code_completion.png)

Code completion that tells you where variables come from, what type they are, array dimensions and much more

# Signature help

![signature_help](/assets/lsp_features/signature_help.gif)

Get signature help for calling external modules, including the documentation of the module and the parameter with their documentation.

# Go-to definition

![goto_definition](/assets/lsp_features/goto_definition.gif)

Go-to definition for modules, variables and subroutines

# Find references

![find_references](/assets/lsp_features/find_references.gif)

Find references for variables and modules (modules being in prototype state)

# Rename refactoring

![rename_refactoring](/assets/lsp_features/rename_refactoring.gif)

Renaming of variables and subroutines

# Quickfixes

![quickfixes](/assets/lsp_features/quickfixes.gif)

Many builtin diagnostics already include quickfixes

Some of them do more than one modification:

![extensive_quickfix](/assets/lsp_features/extensive_quickfix.gif)

# Suggest USING

![suggest_using](/assets/lsp_features/suggest_using.gif)

Which Natural developer never forgot to add a using when copy pasting code?

# Extensive Snippets

![extensive_snippets](/assets/lsp_features/extensive_snippets.gif)

Define snippets which depend on variables of usings which get added on the fly (if not already present)

# Outline

![outline](/assets/lsp_features/outline.png)

Get a quick overview of a module with the outline

# Document symbols

![document_symbols](/assets/lsp_features/document_symbols.gif)

Quickly navigate through symbols in the current module

# Workspace symbols

![workspace_symbols](/assets/lsp_features/workspace_symbols.gif)

Quickly find modules by their referable name
