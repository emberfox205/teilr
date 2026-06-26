fu# Teilr — Class Diagram
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
        EC["ExpenseController ✅"]:::coded
        FC["FriendshipController ✅"]:::coded
    end

    subgraph SERVICE ["⚙️ Service Layer"]
        GS["GroupService ✅"]:::coded
        US["UserService ✅"]:::coded
        ES["ExpenseService ✅"]:::coded
        FS["FriendshipService ✅"]:::coded
    end

    subgraph REPO ["🗄️ Repository Layer"]
        GR["GroupRepository ✅"]:::coded
        GMR["GroupMemberRepository ✅"]:::coded
        UR["UserRepository ✅"]:::coded
        BR["BillRepository ✅"]:::coded
        ER["ExpenseSplitRepository ✅"]:::coded
        FR["FriendshipRepository ✅"]:::coded
    end

    subgraph ENTITY ["📦 Entity Layer"]
        U["User ✅"]:::coded
        G["Group ✅"]:::coded
        GM["GroupMember ✅"]:::coded
        B["Bill ✅"]:::coded
        EX["ExpenseSplit ✅"]:::coded
        F["Friendship ✅"]:::coded
    end

    GC --> GS
    UC --> US
    EC --> ES
    FC --> FS

    GS --> GR
    GS --> GMR
    GS --> UR
    US --> UR
    ES --> BR
    ES --> ER
    FS --> FR
    FS --> UR

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
        - Long id
        - Long groupId
        - Long creatorId
        - String description
        - BigDecimal totalAmount
        - String currency
        - String participantNames
        - String status
        - Instant createdAt
    }

    class ExpenseSplit {
        - Long id
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
        - String status
    }

    User "1" --o "n" GroupMember : joins via
    Group "1" --o "n" GroupMember : has
    Group "1" --> "0..*" Bill : contains
    Group "1" --> "0..*" ExpenseSplit : tracked via
    User "1" --> "0..*" ExpenseSplit : has per group
    User "1" --> "0..*" Friendship : has
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
        <<Service>>
        - BillRepository billRepository
        - ExpenseSplitRepository expenseSplitRepository
        + createEqualBill(request BillCreateRequest) Bill
        + createEqualBill(groupId, creatorId, desc, amount, participantIds, participantNames) Bill
        + getSimplifiedDebts(groupId) List~SimplifiedDebtDTO~
        + getBillsForGroup(groupId) List~Bill~
    }

    class UserRepository {
        <<Repository>>
        + findById(id Long) Optional~User~
        + existsById(id Long) boolean
        + findByUsername(username) Optional~User~
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
        <<Repository>>
        + findByGroupId(groupId Long) List~Bill~
        + save(bill Bill) Bill
        + findById(id Long) Optional~Bill~
        + delete(bill Bill) void
    }

    class ExpenseSplitRepository {
        <<Repository>>
        + findByUserIdAndGroupId(userId, groupId) Optional~ExpenseSplit~
        + findByGroupId(groupId Long) List~ExpenseSplit~
        + save(split ExpenseSplit) ExpenseSplit
    }

    class FriendshipService {
        <<Service>>
        - FriendshipRepository friendshipRepository
        - UserRepository userRepository
        + sendRequest(requesterId, targetId) Friendship
        + acceptRequest(friendshipId, userId) Friendship
        + getFriends(userId) List~User~
        + getPendingRequests(userId) List~Friendship~
    }

    class FriendshipRepository {
        <<Repository>>
        + findByUserIdAAndUserIdB(userIdA, userIdB) Optional~Friendship~
        + findAcceptedByUserId(userId) List~Friendship~
        + findByUserIdBAndStatus(userIdB, status) List~Friendship~
    }

    UserService --> UserRepository
    GroupService --> GroupRepository
    GroupService --> GroupMemberRepository
    GroupService --> UserRepository
    ExpenseService --> BillRepository
    ExpenseService --> ExpenseSplitRepository
    FriendshipService --> FriendshipRepository
    FriendshipService --> UserRepository
```

---

## Layer 4: Controller Endpoints

```mermaid
classDiagram
    direction LR

    class UserController {
        <<Controller - Thymeleaf/HTMX>>
        - UserService userService
        + GET /users/search userId Long
    }

    class GroupController {
        <<Controller - Thymeleaf/HTMX>>
        - GroupService groupService
        + GET /groups/new
        + POST /groups
        + GET /groups userId Long
        + POST /groups/groupId/members userId
        + DELETE /groups/groupId requesterId
    }

    class ExpenseController {
        <<RestController - JSON API>>
        - ExpenseService expenseService
        + GET /api/expenses/group/groupId/bills
        + GET /api/expenses/group/groupId/simplified-debts
        + POST /api/expenses/bill
        + POST /api/expenses/settle
    }

    class FriendshipController {
        <<Controller - HTMX/REST>>
        - FriendshipService friendshipService
        + GET /api/friends userId
        + GET /api/friends/pending userId
        + POST /api/friends/request requesterId targetId
        + POST /api/friends/accept friendshipId userId
    }

    UserController --> UserService
    GroupController --> GroupService
    ExpenseController --> ExpenseService
    FriendshipController --> FriendshipService
```

---

## Layer 5: DTOs (Data Transfer Objects)

```mermaid
classDiagram
    direction LR

    class BillCreateRequest {
        - Long groupId
        - Long creatorId
        - String description
        - BigDecimal totalAmount
        - List~Long~ participantIds
        - String participantNames
    }

    class SettleUpRequest {
        - Long groupId
        - Long debtorId
        - Long creditorId
        - BigDecimal amount
    }

    class SimplifiedDebtDTO {
        - Long debtorId
        - Long creditorId
        - BigDecimal amount
    }

    ExpenseController ..> BillCreateRequest : receives
    ExpenseController ..> SettleUpRequest : receives
    ExpenseController ..> SimplifiedDebtDTO : returns
```