# Project instructions

## GitHub issues

Create issues only from a template in `.github/ISSUE_TEMPLATE/`
(`bug`, `taak`, `user-story`, `overleg`, `epic`). Blank issues are disabled
in the GitHub UI, but `gh` and the API can still bypass that, so follow these
rules:

1. One problem or one piece of work per issue. Pick the template that fits
   and read its YAML file.
2. Title: describe the problem or the work, not the solution, in about ten
   words. No prefix such as `[Bug]`; the issue type already says that.
3. Set the issue type from the template's `type` key (`Bug`, `Task`,
   `Feature`, `Epic`, `Overleg`). Do not use labels for the kind of issue.
4. Add a label for every part the issue touches. Use only these labels:
   - module: `server`, `android`, `shared`, `demo-frontend`
   - shared app package: `core:netwerk`, `core:sessie`, `core:navigatie`,
     `core:thema`, `core:fouten`
   - feature package: `product`, `reservation`, `security`, `pricing`
   - build and project setup: `config`
5. Write the body in Dutch, with one `### <label>` heading per field, in the
   template's order, and the answer below it. This matches how GitHub renders
   a submitted issue form. In a bug, keep observations and the suspected
   cause in their own fields.
6. If a required field is unknown, ask the user before creating the issue.
   Never invent content to fill it. Leave an optional field out when it has
   no answer.
7. Link a user story to its epic as a sub-issue, not as text in the body.
8. Show the title, type, labels and body to the user and wait for approval
   before creating the issue.

`gh issue create` cannot set an issue type in older `gh` versions. Create the
issue through the REST API instead:

```bash
gh api repos/renee77/restpartijen-api/issues \
  -f title="..." -f body="..." -f type="Bug" -f "labels[]=server"
```

## Pull requests

Every pull request follows `.github/pull_request_template.md`. Write the
body in Dutch, like the issues.

1. Fill in every heading of the template. Never leave a heading empty;
   remove an optional heading ("Bekend en geaccepteerd") only when there is
   nothing to say, and say "niet van toepassing" with a reason for a
   checklist item that does not apply.
2. Under "Issue", add one `Closes #<number>` line per issue that this pull
   request completes, so GitHub closes the issue on merge and the epic
   stays up to date. Use `Closes` only when every item under "Klaar als"
   is done; otherwise write `Deel van #<number>` so the issue stays open.
   Find the issue with `gh issue list` before writing the body; ask the
   user when it is unclear which issue the work belongs to.
3. Tick only what you actually checked, with the evidence in the text
   (command and result). Leave a box unticked and say why when a check was
   not run.
4. The title follows Conventional Commits in English, like the commits.
5. Keep any attribution lines that the session instructions require at the
   top of the body.
6. Show the title and body to the user and wait for approval before
   creating the pull request.
