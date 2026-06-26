# Teilr — Database Reference

Database: **MySQL** (configured in `application.properties`).
ORM: **Spring Data JPA / Hibernate** — schema is auto-managed via `spring.jpa.hibernate.ddl-auto`.

---

## Table: `users`

**Entity:** [`User.java`](src/main/java/frauas/teilr/entity/User.java)
**Repository:** [`UserRepository`](src/main/java/frauas/teilr/repository/UserRepository.java)
**Used by:** `UserService`, `GroupService`, `FriendshipService`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | PK, **not** auto-generated | 4-digit code (0000–9999), assigned by the user on registration |
| `username` | `VARCHAR` | | Unique handle used for friend search (`@username`) |
| `email` | `VARCHAR` | | |
| `password_hash` | `VARCHAR` | | **Never store plaintext.** Store bcrypt hash only. |

**Accessed by these endpoints**

| Endpoint | Operation |
|---|---|
| `GET /api/users/search?userId=` | `SELECT * WHERE id = ?` |
| `GET /api/friends?userId=` | `SELECT * WHERE id IN (friend IDs)` |
| `GET /api/groups/{id}/members` (internal) | `SELECT * WHERE id IN (member IDs)` |

---

## Table: `user_groups`

> Named `user_groups` to avoid the SQL reserved word `GROUP`.

**Entity:** [`Group.java`](src/main/java/frauas/teilr/entity/Group.java)
**Repository:** [`GroupRepository`](src/main/java/frauas/teilr/repository/GroupRepository.java)
**Used by:** `GroupService`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | |
| `name` | `VARCHAR` | | Display name of the group |
| `admin_id` | `BIGINT` | FK → `users.id` (logical) | Only this user may delete the group |

**Accessed by these endpoints**

| Endpoint | Operation |
|---|---|
| `POST /api/groups` | `INSERT` |
| `GET /api/groups?userId=` | `SELECT` via `group_members` join (in-memory) |
| `DELETE /api/groups/{groupId}` | `DELETE WHERE id = ?` (admin check first) |

---

## Table: `group_members`

Join table — one row per user-group membership.

**Entity:** [`GroupMember.java`](src/main/java/frauas/teilr/entity/GroupMember.java)
**Repository:** [`GroupMemberRepository`](src/main/java/frauas/teilr/repository/GroupMemberRepository.java)
**Used by:** `GroupService`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | |
| `group_id` | `BIGINT` | FK → `user_groups.id` | |
| `user_id` | `BIGINT` | FK → `users.id` | |
| *(unique)* | | `UNIQUE(group_id, user_id)` | Prevents duplicate membership |

**Accessed by these endpoints**

| Endpoint | Operation |
|---|---|
| `POST /api/groups` | `INSERT` (admin + each memberIds) |
| `GET /api/groups?userId=` | `SELECT WHERE user_id = ?` (returns groups) |
| `POST /api/groups/{groupId}/members` | `INSERT` (guard: exists check first) |
| `DELETE /api/groups/{groupId}` | cascade-deleted when group is deleted |

---

## Table: `bills`

One row per bill created (including settle-up transactions).

**Entity:** [`Bill.java`](src/main/java/frauas/teilr/entity/Bill.java)
**Repository:** [`BillRepository`](src/main/java/frauas/teilr/repository/BillRepository.java)
**Used by:** `ExpenseService`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | |
| `group_id` | `BIGINT` | NOT NULL | |
| `creator_id` | `BIGINT` | NOT NULL | User who paid the bill upfront |
| `description` | `VARCHAR` | NOT NULL | e.g. `"Dinner"` or `"Settle Up"` |
| `total_amount` | `DECIMAL(10,2)` | | |
| `currency` | `VARCHAR` | default `"EUR"` | Hard-coded for now |
| `participant_names` | `VARCHAR` | | Denormalised display string — avoids a join on every bill card render |
| `status` | `VARCHAR` | default `"COMPLETED"` | Reserved for future PENDING/DISPUTED states |
| `created_at` | `TIMESTAMP` | not updatable, default `now()` | Set at entity construction time via `Instant.now()` |

**Accessed by these endpoints**

| Endpoint | Operation |
|---|---|
| `POST /api/expenses/bill` | `INSERT` |
| `POST /api/expenses/settle` | `INSERT` (settle-up is stored as a bill) |
| `GET /api/expenses/group/{groupId}/bills` | `SELECT WHERE group_id = ?` |

---

## Table: `expense_splits`

Running balance ledger — **one row per user per group**, updated cumulatively with every bill.

**Entity:** [`ExpenseSplit.java`](src/main/java/frauas/teilr/entity/ExpenseSplit.java)
**Repository:** [`ExpenseSplitRepository`](src/main/java/frauas/teilr/repository/ExpenseSplitRepository.java)
**Used by:** `ExpenseService`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | |
| `user_id` | `BIGINT` | NOT NULL | |
| `group_id` | `BIGINT` | NOT NULL | |
| `total_owed` | `DECIMAL(10,2)` | default `0.00` | Running sum of what this user owes across all bills |
| `total_paid` | `DECIMAL(10,2)` | default `0.00` | Running sum of what this user has paid upfront |
| *(unique)* | | `UNIQUE(user_id, group_id)` | Exactly one ledger row per user per group |

**Derived column (computed in Java, not stored)**

```
balance = total_paid − total_owed
  > 0  → group owes this user (creditor)
  < 0  → this user owes the group (debtor)
  = 0  → settled
```

**Accessed by these endpoints**

| Endpoint | Operation |
|---|---|
| `POST /api/expenses/bill` | `SELECT` then `UPDATE` (upsert via `getOrCreate`) — one `saveAll` batch |
| `POST /api/expenses/settle` | same as above — settle-up adjusts balances |
| `GET /api/expenses/group/{groupId}/simplified-debts` | `SELECT WHERE group_id = ?`, then debt-simplification algorithm runs in-memory |

> **Note:** The simplified-debt algorithm mutates entity fields in-memory to track running balances during the loop. It does **not** flush those mutations back to the database — the `@Transactional` annotation is intentionally absent on `getSimplifiedDebts`.

---

## Table: `friendships`

One row per directed friend request.

**Entity:** [`Friendship.java`](src/main/java/frauas/teilr/entity/Friendship.java)
**Repository:** [`FriendshipRepository`](src/main/java/frauas/teilr/repository/FriendshipRepository.java)
**Used by:** `FriendshipService`

| Column | Type | Constraints | Notes |
|---|---|---|---|
| `id` | `BIGINT` | PK, AUTO_INCREMENT | |
| `user_id_a` | `BIGINT` | NOT NULL | The user who **sent** the request |
| `user_id_b` | `BIGINT` | NOT NULL | The user who **received** the request |
| `status` | `VARCHAR` | NOT NULL, default `"PENDING"` | `"PENDING"` or `"ACCEPTED"` |
| *(unique)* | | `UNIQUE(user_id_a, user_id_b)` | One direction only; service guards the reverse |

**Direction matters:** the service always checks both `(A,B)` and `(B,A)` before inserting, so duplicate/mirrored requests are blocked in application logic even though the DB constraint only covers one direction.

**Accessed by these endpoints**

| Endpoint | Operation |
|---|---|
| `POST /api/friends/request` | `SELECT` (duplicate check) + `INSERT` |
| `POST /api/friends/accept` | `SELECT WHERE id = ?` + `UPDATE status = 'ACCEPTED'` |
| `GET /api/friends?userId=` | `SELECT WHERE (user_id_a = ? OR user_id_b = ?) AND status = 'ACCEPTED'` |
| `GET /api/friends/pending?userId=` | `SELECT WHERE user_id_b = ? AND status = 'PENDING'` |

---

## Entity Relationship Summary

```
users ──< group_members >── user_groups
users ──< expense_splits
users ──< friendships (as sender or recipient)
user_groups ──< bills
user_groups ──< expense_splits
```