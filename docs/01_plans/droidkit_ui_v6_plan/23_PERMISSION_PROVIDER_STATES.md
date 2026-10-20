# 23 — Permission & Provider States

Unify UI around existing permission/capability/provider sources.

Flow:
Tool -> evaluate requirements -> ready OR explanatory blocked state -> user action -> return/resume -> re-evaluate -> update UI.

Handle denied and permanently denied permission states when Android APIs allow reliable distinction. Provider grant/connect flows must refresh after return. Avoid permission requests at startup; request in context.
