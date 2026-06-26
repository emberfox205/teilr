# Teilr — Frontend API Reference

Base URL: `http://localhost:8080`

Two transports are in use:
- **HTMX** endpoints return a rendered Thymeleaf HTML fragment — swap it directly into the DOM with `hx-target`.
- **REST** endpoints return JSON — use `fetch` / `axios` / your preferred HTTP client.

---

## Users

### Register a new user

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/users/register` |
| **Transport** | Form Submit (HTMX) |
| **Body** | `application/x-www-form-urlencoded` |
| **Response** | `302 Redirect` to `/ui/home` on success, or `/ui/register?error=true` |

**Parameters**

| Param | Required | Notes |
|---|---|---|
| `username` | ✅ | User handle |
| `email` | ✅ | Unique email |
| `passwordHash` | ✅ | The raw password to be hashed |

---

### Log in

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/users/login` |
| **Transport** | Form Submit (HTMX) |
| **Body** | `application/x-www-form-urlencoded` |
| **Response** | `302 Redirect` to `/ui/home` on success, or `/ui/login?error=true` |

**Parameters**

| Param | Required | Notes |
|---|---|---|
| `identifier` | ✅ | Email or 4-digit ID |
| `passwordHash` | ✅ | The raw password |

---

### Log out

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/users/logout` |
| **Transport** | Form Submit (HTMX) |
| **Response** | `302 Redirect` to `/ui/login` |

---

### Search for a user by ID

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/users/search?userId={id}` |
| **Transport** | HTMX |
| **Fragment** | `fragments/user-search-result :: searchResultContent` |

**HTMX usage**
```html
<input type="number" name="userId"
       hx-get="/api/users/search"
       hx-trigger="input changed delay:300ms"
       hx-target="#search-result"
       hx-include="[name='userId']">
<div id="search-result"></div>
```

**Template variables exposed**

| Variable | Type | Notes |
|---|---|---|
| `user` | `User` \| `null` | `null` when not found |
| `notFound` | `boolean` | `true` when no match |

---

## Groups

### Load the group creation form

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/groups/new` |
| **Transport** | HTMX |
| **Fragment** | `fragments/group-form :: groupFormContent` |

```html
<button hx-get="/api/groups/new" hx-target="#modal-content">New Group</button>
```

---

### Create a group

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/groups` |
| **Transport** | HTMX (form submit) |
| **Body** | `application/x-www-form-urlencoded` |
| **Fragment** | `fragments/group-card :: groupCardContent` |

**Parameters**

| Param | Required | Notes |
|---|---|---|
| `name` | ✅ | Group display name |
| `adminId` | ✅ | ID of the creating user — becomes admin |
| `memberIds` | ❌ | Repeat for multiple: `memberIds=1&memberIds=2`. Admin is always added, even if omitted. |

```html
<form hx-post="/api/groups" hx-target="#group-list" hx-swap="beforeend">
  <input name="name" placeholder="Trip name">
  <input name="adminId" type="hidden" value="{{currentUserId}}">
  <!-- tick-list from /api/friends populates memberIds -->
  <button type="submit">Create</button>
</form>
```

**Template variables exposed**

| Variable | Type |
|---|---|
| `group` | `Group` |
| `members` | `List<User>` |

---

### List all groups for a user

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/groups?userId={id}` |
| **Transport** | HTMX |
| **Fragment** | `fragments/group-list :: groupListContent` |

```html
<div hx-get="/api/groups?userId={{currentUserId}}"
     hx-trigger="load"
     hx-target="#group-list"></div>
```

---

### Add a member to a group

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/groups/{groupId}/members?userId={id}` |
| **Transport** | HTMX |
| **Fragment** | `fragments/member-list :: memberListContent` |

```html
<button hx-post="/api/groups/{{groupId}}/members?userId={{userId}}"
        hx-target="#member-list">Add</button>
```

---

### Delete a group *(admin only)*

| | |
|---|---|
| **Method** | `DELETE` |
| **URL** | `/api/groups/{groupId}?requesterId={id}` |
| **Transport** | HTMX |
| **Response** | `204 No Content` — HTMX removes the element |

```html
<button hx-delete="/api/groups/{{groupId}}?requesterId={{currentUserId}}"
        hx-target="closest .group-card"
        hx-swap="outerHTML">Delete Group</button>
```

> ⚠️ Throws `403 SecurityException` if the caller is not the group admin.

---

## Friends

### List accepted friends

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/friends?userId={id}` |
| **Transport** | HTMX |
| **Fragment** | `fragments/friend-list :: friendListContent` |
| **Primary use** | Populate the member tick-list in the "Create Group" form |

```html
<div hx-get="/api/friends?userId={{currentUserId}}"
     hx-trigger="load"
     hx-target="#friend-list"></div>
```

**Template variables**

| Variable | Type |
|---|---|
| `friends` | `List<User>` |

---

### List incoming friend requests

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/friends/pending?userId={id}` |
| **Transport** | HTMX |
| **Fragment** | `fragments/friend-requests :: requestListContent` |

```html
<div hx-get="/api/friends/pending?userId={{currentUserId}}"
     hx-trigger="load"
     hx-target="#pending-requests"></div>
```

**Template variables**

| Variable | Type |
|---|---|
| `requests` | `List<Friendship>` — each has `.id`, `.userIdA` (sender), `.status` |

---

### Send a friend request

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/friends/request?requesterId={id}&targetId={id}` |
| **Transport** | REST (JSON response) |
| **Response** | `200 Friendship` |

```js
await fetch(`/api/friends/request?requesterId=${me}&targetId=${them}`, {
  method: 'POST'
});
```

> ⚠️ Returns `500` if a request already exists in either direction, or if `requesterId === targetId`. Check and handle gracefully.

---

### Accept a friend request

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/friends/accept?friendshipId={id}&userId={id}` |
| **Transport** | REST (JSON response) |
| **Response** | `200 Friendship` with `status: "ACCEPTED"` |

```js
await fetch(`/api/friends/accept?friendshipId=${reqId}&userId=${me}`, {
  method: 'POST'
});
```

> ⚠️ Only the recipient (`userIdB`) may accept. Returns `403` otherwise.

---

## Expenses

### Get all bills for a group

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/expenses/group/{groupId}/bills` |
| **Transport** | REST (JSON) |
| **Response** | `200 List<Bill>` |

```js
const bills = await fetch(`/api/expenses/group/${groupId}/bills`).then(r => r.json());
```

**Bill fields**

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | |
| `groupId` | `Long` | |
| `creatorId` | `Long` | Who paid upfront |
| `description` | `String` | |
| `totalAmount` | `BigDecimal` | |
| `currency` | `String` | Always `"EUR"` currently |
| `participantNames` | `String` | Comma-separated display string for UI |
| `status` | `String` | Always `"COMPLETED"` currently |
| `createdAt` | `Instant` | ISO-8601 |

---

### Get simplified debts ("who owes who")

| | |
|---|---|
| **Method** | `GET` |
| **URL** | `/api/expenses/group/{groupId}/simplified-debts` |
| **Transport** | REST (JSON) |
| **Response** | `200 List<SimplifiedDebtDTO>` |

```js
const debts = await fetch(`/api/expenses/group/${groupId}/simplified-debts`).then(r => r.json());
// e.g. [{ debtorId: 1, creditorId: 2, amount: 45.00 }]
```

Each entry means: `debtorId` owes `creditorId` exactly `amount`. The algorithm minimises the number of transactions — N members might settle with as few as N-1 transfers.

---

### Create a bill (equal split)

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/expenses/bill` |
| **Transport** | REST (JSON) |
| **Body** | `application/json` — `BillCreateRequest` |
| **Response** | `201 Bill` |

```js
await fetch('/api/expenses/bill', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    groupId: 7,
    creatorId: 1,          // who paid
    description: "Dinner",
    totalAmount: 90.00,
    participantIds: [1, 2, 3],   // everyone who ate
    participantNames: "Alice, Bob, Carol"  // display string for the bill card
  })
});
```

> **Rounding**: The remainder cent(s) (e.g. 100 ÷ 3 = 33.33 + 0.01 leftover) is assigned to `participantIds[0]`.

---

### Settle up (pay off a debt)

| | |
|---|---|
| **Method** | `POST` |
| **URL** | `/api/expenses/settle` |
| **Transport** | REST (JSON) |
| **Body** | `application/json` — `SettleUpRequest` |
| **Response** | `200 Bill` |

```js
await fetch('/api/expenses/settle', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    groupId: 7,
    debtorId: 2,    // person paying
    creditorId: 1,  // person receiving
    amount: 45.00
  })
});
```

Internally reuses `createEqualBill` — the settlement is recorded as a bill where the debtor is the creator and the creditor is the sole participant.
