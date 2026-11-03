# Branch and teamwork rules

## Branches

| Branch | Purpose |
|---|---|
| `main` | Always compiles and passes `mvn test`. Nobody commits directly to it. |
| `feature/vehicles-sessions` | Person A |
| `feature/infrastructure-reservations` | Person B |
| `feature/strategies-manager` | Person C |
| `feature/simulation-reports` | Person D |

Need a smaller piece of work? Create `feature/<short-name>` from `main`.

## Daily workflow

1. Get the latest code: `git checkout main` then `git pull`.
2. Go to your branch: `git checkout feature/your-branch` then `git merge main`.
3. Code and write tests in your own packages.
4. Run `mvn test`. Do not push if it fails.
5. Commit with a clear message: `git commit -m "Add ChargingPort availability check"`.
6. Push: `git push origin feature/your-branch`.
7. Open a pull request into `main`. At least one teammate reviews it before merging.

## Rules

- Only edit files in your own packages. If you need a change in someone else's class, ask them or agree on it in the group chat.
- Never change an agreed class signature (method names, parameters, return types) alone. Discuss it with the team first.
- Every class you write gets a JUnit test in the matching package under `src/test/java`.
- Small, frequent commits are better than one huge commit at the end.
- Pull from `main` often so conflicts stay small.
- If you used generative AI for code, tests or text, note it in the report.

## Package ownership

| Package | Owner |
|---|---|
| `vehicle`, `session` | Person A |
| `infrastructure`, `reservation` | Person B |
| `strategy`, `manager` | Person C |
| `simulation`, `report`, `common`, root files (`pom.xml`, `README.md`) | Person D |
