# Teilr — Class Diagram
> ✅ = currently coded · 🔲 = planned (not yet implemented)

---

## Layer 1: Architecture Overview

```mermaid
flowchart TB
    classDef coded fill:#d4edda,stroke:#28a745,color:#000
    classDef planned fill:#f0f0f0,stroke:#aaaaaa,color:#888,stroke-dasharray: 5 5

    subgraph CONTROLLER ["🌐 Controller Layer"]
        GC["GroupController ✅"]:::coded
        UC["UserController ✅"]:::coded
        EC["ExpenseController 🔲"]:::planned
    end

    subgraph SERVICE ["⚙️ Service Layer"]
        GS["GroupService ✅"]:::coded
        US["UserService ✅"]:::coded
        ES["ExpenseService 🔲"]:::planned
    end

    subgraph REPO ["🗄️ Repository Layer"]
        GR["GroupRepository ✅"]:::coded
        GMR["GroupMemberRepository ✅"]:::coded
        UR["UserRepository ✅"]:::coded
        BR["BillRepository 🔲"]:::planned
        ER["ExpenseSplitRepository 🔲"]:::planned
        FR["FriendshipRepository 🔲"]:::planned
    end

    subgraph ENTITY ["📦 Entity Layer"]
        U["User ✅"]:::coded
        G["Group ✅"]:::coded
        GM["GroupMember ✅"]:::coded
        B["Bill 🔲"]:::planned
        EX["ExpenseSplit 🔲"]:::planned
        F["Friendship 🔲"]:::planned
    end

    GC --> GS
    UC --> US
    EC --> ES

    GS --> GR
    GS --> GMR
    GS --> UR
    US --> UR
    ES --> BR
    ES --> ER
    ES --> GR

    GR -.->|returns| G
    GMR -.->|returns| GM
    UR -.->|returns| U
    BR -.->|returns| B
    ER -.->|returns| EX
    FR -.->|returns| F
```

---

## Layer 2: Entity Relationships

```mermaid
classDiagram
    direction TB

    class User {
        - Long id
        - String username
        - String email
        - String passwordHash
    }

    class Group {
        - Long id
        - String name
        - Long adminId
    }

    class GroupMember {
        - Long id
        - Long groupId
        - Long userId
    }

    class Bill {
        <<planned>>
        - Long id
        - Long groupId
        - Long creatorId
        - String description
        - BigDecimal totalAmount
        - String currency
        - BillStatus status
        - Instant createdAt
    }

    class BillStatus {
        <<planned>>
        DRAFT
        SUBMITTED
    }

    class ExpenseSplit {
        <<planned>>
        - Long userId
        - Long groupId
        - BigDecimal totalOwed
        - BigDecimal totalPaid
        + getBalance() BigDecimal
    }

    class Friendship {
        <<planned>>
        - Long userIdA
        - Long userIdB
        - FriendshipStatus status
    }

    class FriendshipStatus {
        <<planned>>
        PENDING
        ACCEPTED
    }

    User "1" --o "n" GroupMember : joins via
    Group "1" --o "n" GroupMember : has
    Group "1" --> "0..*" Bill : contains
    Group "1" --> "0..*" ExpenseSplit : debt tracked via
    User "1" --> "0..*" ExpenseSplit : has per group
    User "1" --> "0..*" Friendship : has
    Bill --> BillStatus : has
    Friendship --> FriendshipStatus : has
```

---

## Layer 3: Service & Repository Detail

```mermaid
classDiagram
    direction LR

    class UserService {
        <<Service>>
        - UserRepository userRepository
        + findById(id Long) Optional~User~
        + save(user User) User
    }

    class GroupService {
        <<Service>>
        - GroupRepository groupRepository
        - GroupMemberRepository groupMemberRepository
        - UserRepository userRepository
        + createGroup(name, adminId, memIds) Group
        + findGroupById(groupId) Optional~Group~
        + addMember(groupId, userId) void
        + removeMember(groupId, userId) void
        + getGroupsForUser(userId) List~Group~
        + getMembersOfGroup(groupId) List~User~
        + deleteGroup(groupId, requestedId) void
    }

    class ExpenseService {
        <<planned>>
        + createBill(groupId, creatorId, desc, amount) Bill
        + removeBill(billId, requesterId) void
        + getBillsForGroup(groupId) List~Bill~
        + applyBillToSplits(bill) void
        + getDebtSummary(userId, groupId) ExpenseSplit
    }

    class UserRepository {
        <<Repository>>
        + findById(id Long) Optional~User~
        + existsById(id Long) boolean
        + save(user User) User
        + delete(user User) void
    }

    class GroupRepository {
        <<Repository>>
        + findById(id Long) Optional~Group~
        + save(group Group) Group
        + delete(group Group) void
    }

    class GroupMemberRepository {
        <<Repository>>
        + findByGroupId(groupId Long) List~GroupMember~
        + findByUserId(userId Long) List~GroupMember~
        + existsByGroupIdAndUserId(groupId, userId) boolean
        + deleteByGroupIdAndUserId(groupId, userId) void
    }

    class BillRepository {
        <<planned>>
        + findById(id Long) Bill
        + findByGroupId(groupId) List~Bill~
        + save(bill Bill) Bill
        + delete(id Long) void
    }

    class ExpenseSplitRepository {
        <<planned>>
        + findByUserIdAndGroupId(uid, gid) ExpenseSplit
        + findByGroupId(groupId) List~ExpenseSplit~
        + save(split ExpenseSplit) ExpenseSplit
    }

    class FriendshipRepository {
        <<planned>>
        + findByUserId(userId) List~Friendship~
        + findByUserIdAAndUserIdB(a, b) Friendship
        + save(friendship Friendship) Friendship
    }

    UserService --> UserRepository
    GroupService --> GroupRepository
    GroupService --> GroupMemberRepository
    GroupService --> UserRepository
    ExpenseService --> BillRepository
    ExpenseService --> ExpenseSplitRepository
    ExpenseService --> GroupRepository
```

---

## Layer 4: Controller Endpoints

```mermaid
classDiagram
    direction LR

    class UserController {
        <<Controller>>
        - UserService userService
        + GET /users/search userId Long
    }

    class GroupController {
        <<Controller>>
        - GroupService groupService
        + GET /groups/new
        + POST /groups
        + GET /groups userId Long
        + POST /groups/groupId/members userId
        + DELETE /groups/groupId requesterId
    }

    class ExpenseController {
        <<planned>>
        + GET /expenses/new groupId
        + POST /expenses
        + GET /expenses/summary userId groupId
        + GET /expenses groupId
    }

    UserController --> UserService
    GroupController --> GroupService
    ExpenseController --> ExpenseService
```