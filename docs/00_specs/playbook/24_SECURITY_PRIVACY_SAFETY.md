# Security, Privacy, and Safety Foundation

## Data minimization
- Collect/store only what a feature needs.
- Avoid persistent device identifiers unless necessary and justified.
- Reports show what will be exported.
- Local-first by default.
- Provide deletion of local diagnostic history.

## Secrets
- No secrets in source control.
- ADB/private keys use Android secure storage appropriate to the implementation.
- Do not log secrets or sensitive command output.

## Privileged operations
- Capability check immediately before execution.
- Validate target and parameters.
- Prefer allow-listed structured operations.
- Separate read-only from mutating operations.
- Risk levels drive confirmation UX.
- Return exact success/failure result.

## Risk levels
0 Safe/read-only
1 Minor/navigation
2 System change
3 High impact/destructive
4 Advanced/root

## Prohibited product areas
No FRP bypass, credential theft/bypass, lock bypass, stealth surveillance, persistence, unauthorized access, or exploit delivery.

## Privacy review gate
- [ ] Data inventory
- [ ] Storage locations
- [ ] Exported fields
- [ ] Retention/deletion
- [ ] Permissions justified
- [ ] Sensitive logs reviewed
- [ ] Distribution privacy requirements satisfied
