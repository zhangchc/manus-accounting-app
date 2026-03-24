#!/usr/bin/env bash
set -euo pipefail

REPO_DIR="/Users/zhangchuancun/manus-accounting-app"
TARGET_BRANCH="blue"
COMMIT_MSG="${1:-chore: sync local changes}"

cd "$REPO_DIR"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "Error: 当前目录不是 git 仓库: $REPO_DIR"
  exit 1
fi

echo "==> 当前分支: $(git branch --show-current)"
echo "==> 目标远程分支: $TARGET_BRANCH"

git add -A

if ! git diff --cached --quiet; then
  git commit -m "$COMMIT_MSG"
  echo "==> 已提交: $COMMIT_MSG"
else
  echo "==> 没有新的变更可提交，跳过 commit"
fi

git push origin HEAD:"$TARGET_BRANCH"
echo "==> 推送完成: origin/$TARGET_BRANCH"
