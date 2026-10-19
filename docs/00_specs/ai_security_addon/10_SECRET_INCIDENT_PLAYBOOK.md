# Secret Incident Playbook

If a credential is exposed:
1. Stop using it.
2. Revoke/rotate it at the provider.
3. Find every exposure: file, commit, PR, Actions log, release, screenshot, issue.
4. Put the replacement only in approved local/deployment secret storage.
5. Remove it from tracked files.
6. If committed, remediate history according to repository policy and coordinate before rewriting shared history.
7. Invalidate affected artifacts where necessary.
8. Review provider usage/logs for abuse.
9. Improve scanning/ignore rules.
10. Document the incident without copying the secret.

History cleanup never replaces rotation. A published secret is assumed compromised.
