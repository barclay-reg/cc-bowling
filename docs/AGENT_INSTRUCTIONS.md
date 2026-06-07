# Agent Instructions

Guidelines for AI agents (GitHub Copilot, etc.) working on this project.

## Commit Message Format

Use [Conventional Commits](https://www.conventionalcommits.org/) format for all commits.

### Commit Types

The following commit types are used in this project:

| Type | Purpose | Example |
|------|---------|---------|
| **feat** | A new feature | `feat: add strike bonus calculation` |
| **fix** | A bug fix | `fix: correct spare bonus calculation` |
| **docs** | Documentation only changes | `docs: add glossary for bowling terms` |
| **build** | Changes that affect the build system or dependencies | `build: upgrade Spring Boot to 3.0.0` |
| **ci** | Changes to CI/CD configuration | `ci: add GitHub Actions workflow` |
| **chore** | Other changes that don't modify src or test files | `chore: update .gitignore` |

### Commit Message Structure

```
<type>(<scope>): <subject>

<body>

<footer>
```

- **type**: One of the types listed above
- **scope** (optional): Component or area affected (e.g., `scoring`, `frame`, `api`)
- **subject**: Concise description in imperative mood, lowercase, no period
- **body** (optional): Detailed explanation of changes
- **footer** (optional): Breaking changes, issue references

### Example Commits

```
feat(scoring): add spare bonus calculation

Implement logic to calculate spare bonuses by adding the next roll to the frame score.

Closes #42
```

```
docs: add agent instructions

Add guidelines for conventional commits and development practices.

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

## Co-authoring with AI

When working with GitHub Copilot, include the co-authoring trailer:

```
Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>
```

This trailer should be placed at the end of the commit message on its own line.

## Code Quality Standards

- Follow existing code style and conventions
- Write clear, concise code with minimal comments
- Ensure all tests pass before committing
- Update documentation when making significant changes

## Domain Knowledge

Refer to [GLOSSARY.md](./concepts/GLOSSARY.md) for business-relevant terminology and concepts related to the bowling domain.
