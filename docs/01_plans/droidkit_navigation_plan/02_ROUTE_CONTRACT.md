# DroidKit Route Contract

## Principle
A route pattern is not automatically a navigable destination string.

Examples:
- Pattern: `sensors/live/{type}`
- Concrete destination: `sensors/live/1`

ToolRegistry entries used as direct navigation targets must resolve to concrete entry destinations. If a feature needs an argument chosen by the user, the registry entry should lead to a safe selection/index screen first or use an explicit route builder with a real argument.

## Required route metadata audit
For every destination record:
- route pattern
- route builder/function
- required args
- optional/default args
- source destinations
- intended parent/up destination
- whether it is allowed in ToolRegistry
- whether it can be deep-linked later

## Invariants
1. No direct ToolRegistry entry contains unresolved required placeholders.
2. Required arguments are validated before navigation.
3. Invalid/missing arguments produce a safe UI state or controlled navigation, never an uncaught exception.
4. Navigation constants/builders have one source of truth.
5. Feature modules remain isolated; app shell composes graphs.
6. Back-stack manipulation is documented for nontrivial flows.
