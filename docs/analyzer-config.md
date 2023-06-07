# Configuration

The following configurations can be set in a `.editorconfig` file to configure preferences for analyzers.

| Property | Possible values | Default value |
| --- | --- | --- |
| `natls.style.comparisons` | `sign` (`<`, `=`, etc.), `short` (`LT`, `EQ`, etc.) | `sign` |

# Example

```editorconfig
[*]
natls.style.comparisons=sign
[**/*.NSC]
natls.style.comparisons=short
```
