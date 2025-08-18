# Configuration

The following configurations can be set in a `.editorconfig` file to configure preferences for analyzers. Note, that **all** configurations are set to false by default, so you have to turn them on individually according to the below table.

| Property                                      | Possible values | Description |
|-----------------------------------------------| --- | --- |
| `natls.style.comparisons`                     | `sign`, `short`, `false` | [`NL006`](../tools/ruletranslator/src/main/resources/rules/NL006)|
| `natls.style.disallowtoplevelvars`            | `true`, `false` | [`NL018`](../tools/ruletranslator/src/main/resources/rules/NL018)|
| `natls.style.qualifyvars`                     | `true`, `false` | [`NL019`](../tools/ruletranslator/src/main/resources/rules/NL019)|
| `natls.style.discourage_independent`          | `true`, `false` | [`NL028`](../tools/ruletranslator/src/main/resources/rules/NL028)|
| `natls.style.discourage_gitmarkers`           | `true`, `false` | [`NL030`](../tools/ruletranslator/src/main/resources/rules/NL030)|
| `natls.style.discourage_inlineparameters`     | `true`, `false` | [`NL031`](../tools/ruletranslator/src/main/resources/rules/NL031)|
| `natls.style.discourage_hiddentransactions`   | `true`, `false` | [`NL032`](../tools/ruletranslator/src/main/resources/rules/NL032)|
| `natls.style.discourage_hiddenworkfiles`      | `true`, `false` | [`NL033`](../tools/ruletranslator/src/main/resources/rules/NL033)|
| `natls.style.mark_mainframelongline`          | `true`, `false` | [`NL034`](../tools/ruletranslator/src/main/resources/rules/NL034)|
| `natls.style.discourage_hidden_dbms`          | `true`, `false` | [`NL035`](../tools/ruletranslator/src/main/resources/rules/NL035)|
| `natls.style.discourage_long_literals`        | `true`, `false` | [`NL038`](../tools/ruletranslator/src/main/resources/rules/NL038)|
| `natls.style.discourage_lowercase_code`       | `true`, `false` | [`NL039`](../tools/ruletranslator/src/main/resources/rules/NL039)|
| `natls.style.in_out_groups`                   | `true`, `false` | [`NL041`](../tools/ruletranslator/src/main/resources/rules/NL041)|

# Example

```editorconfig
[*]
natls.style.comparisons=sign
[**/*.NSC]
natls.style.comparisons=short
```
