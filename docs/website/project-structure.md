---
title: "System Libraries"
weight: 5
---

Natural provides (sub)programs and data areas that are known to the runtime, but not included in your sources (e.g. user
 exits, RPC stuff etc.).

NatLS does not know about these modules, because they're not part of your source repository.

To resolve this issue, you can create a folder called `include` in your root directory, next to your
`Natural-Libraries`. Inside that folder you can create folders for libraries that were not part of your repository.

## Example

Lets say you have the libraries `LIBONE` and `LIBTWO` and `LIBONE` wants to use RPC features.
Your repository will look like this:

```
.natural
Natural-Libraries/
   LIBONE
   LIBTWO
```

According to the `.natural` file, your `LIBONE` library has `SYSRPC` as a step lib and you can't resolve a LDA called `RPCL` within `LIBONE`.

To let your programs know about `RPCL`, you can create an `include` directory in the root directory of the repository
and put the libraries and their files inside it.

Your repository will now look like this:

```
.natural
include/
   SYSRPC/
       SRC/
           RPCL.NSL
Natural-Libraries/
   LIBONE
   LIBTWO
```

Now the sources in `LIBONE` can resolve the LDA `RPCL`.
