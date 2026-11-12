# Project instructions

## GitHub issues

Create issues only from a template in `.github/ISSUE_TEMPLATE/`
(`bug`, `taak`, `user-story`, `overleg`, `epic`). Blank issues are disabled
in the GitHub UI, but `gh` and the API can still bypass that, so follow these
rules:

1. Pick the template that fits and read its YAML file.
2. Use the template's `title` prefix and its `labels`.
3. Write the body in Dutch, with one `### <label>` heading per field, in the
   template's order, and the answer below it. This matches how GitHub renders
   a submitted issue form.
4. If a required field is unknown, ask the user before creating the issue.
   Never invent content to fill it. Leave an optional field out when it has
   no answer.
5. Show the title and body to the user and wait for approval before running
   `gh issue create`.
