# DB設計書

## 1. 文書の目的 [追加]

この文書は、ToDo管理アプリがデータベース（データを保存する仕組み）へ保存する情報の形を決めるDB設計書です。  
画面の詳しい配置や動きは、別に作成する画面設計書で扱います。

## 2. 設計の対象

- 使用するデータベース管理システムは、MySQL 8.0です。
- 作成するテーブル（データを表形式で保存する入れ物）は `todos` の1つだけです。
- ToDo（やること）1件を、`todos` テーブルの1行として保存します。
- この設計書では、ログの保存用テーブルは作りません。ログ要件はアプリケーション側で扱います。

## 3. テーブル一覧

| テーブル名 | 論理名（分かりやすい名前） | 用途 |
|---|---|---|
| `todos` | やること | やることの内容、分類、優先度、期限、完了状態、登録・更新日時を保存する |

## 4. `todos` テーブル定義

### 4.1 カラム一覧

カラム（テーブルの縦の項目）は以下のとおりです。

| No. | カラム名 | 論理名 | データ型 | NULL（値なし） | 初期値 | 説明 |
|---|---|---|---|---|---|---|
| 1 | `id` | ID | `BIGINT` | 不可 | 自動採番 | 1件を見分ける番号。主キー（各行を重複なく見分ける項目） |
| 2 | `title` | やること | `VARCHAR(255)` | 不可 | なし | やることの名前。255文字以内 |
| 3 | `detail` | メモ | `VARCHAR(255)` | 可 | `NULL` | 補足のメモ。255文字以内 |
| 4 | `category` | ジャンル | `VARCHAR(255)` | 不可 | なし | デザイン、マーケティング、プログラミング、資格、就職活動のいずれか |
| 5 | `priority` | 優先度 | `INT` | 不可 | `2` | `1`=高、`2`=中、`3`=低 |
| 6 | `due_date` | 期限 | `DATE` | 可 | `NULL` | 期限の日付 |
| 7 | `completed` | 完了状態 | `BOOLEAN` | 不可 | `FALSE` | `FALSE`=未完了、`TRUE`=完了。MySQLでは内部的に `0` / `1` として扱う |
| 8 | `created_at` | 登録日時 | `DATETIME` | 不可 | 登録時の日時 | レコード（テーブルの1行）を登録した日時。MySQLが自動で設定 |
| 9 | `updated_at` | 更新日時 | `DATETIME` | 不可 | 登録時の日時 | レコードを最後に更新した日時。MySQLが自動で設定・更新 |

### 4.2 主キー

| 対象カラム | 制約（守る決まり） | 内容 |
|---|---|---|
| `id` | `PRIMARY KEY` | 同じ値を重複して保存できず、各ToDoを一意に見分ける |

### 4.3 入力値の制約 [追加]

叩き台では画面の選択肢として指定されている内容を、データベースにも設定します。これにより、画面以外から保存しようとした場合も、決められていない値を防ぎます。

| 対象カラム | 制約名 | 許可する値 |
|---|---|---|
| `category` | `chk_todos_category` | デザイン / マーケティング / プログラミング / 資格 / 就職活動 |
| `priority` | `chk_todos_priority` | `1` / `2` / `3` |
| `completed` | `chk_todos_completed` | `FALSE` / `TRUE`（MySQLでは `0` / `1`） |

## 5. 要件とカラムの対応

どの要件がどのカラムに保存されるかを示します。

| 要件 | 対応カラム | 保存方法・補足 |
|---|---|---|
| やることを登録・編集する | `title` | 必須入力。255文字以内 |
| メモを登録・編集する | `detail` | 任意入力。未入力時は `NULL` を保存 |
| ジャンルを5つから選択する | `category` | 必須入力。指定の5つの値を保存 |
| 優先度を高・中・低から選択する | `priority` | 高=`1`、中=`2`、低=`3`を保存。初期値は中の`2` |
| 期限を登録・編集する | `due_date` | 任意入力。日付を保存 |
| 完了にチェックを入れる | `completed` | 未完了=`FALSE`、完了=`TRUE`を保存 |
| 一覧にやること・ジャンル・優先度・期限・状態を表示する | `title`、`category`、`priority`、`due_date`、`completed` | `priority` と `completed` は画面表示用の言葉に変換する |
| 名前で絞り込む | `title` | 入力した言葉を一部でも含むデータを探す |
| ジャンルで絞り込む | `category` | 選んだジャンルと同じデータを探す |
| 期限順に並び替える | `due_date` | 期限が近い順または遠い順に並べる |
| 編集・削除する1件を指定する | `id` | URL内の番号と一致する1件を取得する |
| 登録日時を自動記録する | `created_at` | 登録時にMySQLが自動で設定する |
| 更新日時を自動記録する | `updated_at` | 登録時と更新時にMySQLが自動で設定する |
| 操作ログに対象の番号を残す | `id` | ログへ出力する値。ログ専用のテーブルは作らない |

## 6. CREATE TABLE文（DDL）

DDL（データベースのテーブルを作るためのSQL）は以下のとおりです。`todoapp` データベースを選択した状態で実行します。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category CHECK (
        category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')
    ),
    CONSTRAINT chk_todos_priority CHECK (priority IN (1, 2, 3)),
    CONSTRAINT chk_todos_completed CHECK (completed IN (FALSE, TRUE))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 7. 自動設定される値

| カラム名 | いつ設定されるか | 設定内容 |
|---|---|---|
| `id` | 新規登録時 | `AUTO_INCREMENT`（新しい行ごとに番号を自動で1つ増やす仕組み）で番号を設定 |
| `priority` | 値を指定せずに保存した時 | 初期値として `2`（中）を設定 |
| `completed` | 値を指定せずに保存した時 | 初期値として `FALSE`（未完了）を設定 |
| `created_at` | 新規登録時 | その時点の日時を設定 |
| `updated_at` | 新規登録時・更新時 | 新規登録時はその時点の日時を設定し、更新時は更新した日時に変更 |

## 8. 設計上の補足 [追加]

以下は、叩き台・要件定義書の内容を実現するために、このDB設計書で明示した補足です。

| 追加した内容 | 理由 |
|---|---|
| `category`、`priority`、`completed` の `CHECK` 制約 | 決められた選択肢以外の値がデータベースへ保存されることを防ぐため |
| `ENGINE=InnoDB` | MySQLで一般的に使う、データの整合性（データどうしの矛盾がないこと）を保ちやすい保存方式を明示するため |
| `DEFAULT CHARSET=utf8mb4` | ジャンルなどの日本語を正しく保存できる文字コードを明示するため |
| `updated_at` の `ON UPDATE CURRENT_TIMESTAMP` | 更新時に日時をMySQLが自動で書き換える動作をDDLで明確にするため |

## 9. 今回の対象外

- `todos` 以外のテーブルの作成
- 利用者を管理するテーブル
- 削除したデータを保管・復元するテーブル
- 完了日時を保存するカラム
- 写真やファイルを保存するテーブル
