fu# Teilr — Class Diagram
> ✅ = currently coded · 🔲 = planned (not yet implemented)

---

## Layer 1: Architecture Overview

```mermaid
flowchart TB
    classDef coded fill:#d4edda,stroke:#28a745,color:#000
    classDef planned fill:#f0f0f0,stroke:#aaaaaa,color:#888,stroke-dasharray: 5 5

    subgraph CONTROLLER ["🌐 Controller Layer"]
        AC["AuthController ✅"]:::coded
        GC["GroupController ✅"]:::coded
        UC["UserController ✅"]:::coded
        EC["ExpenseController ✅"]:::coded
        FC["FriendshipController ✅"]:::coded
        VC["ViewController ✅"]:::coded
    end

    subgraph SERVICE ["⚙️ Service Layer"]
        GS["GroupService ✅"]:::coded
        US["UserService ✅"]:::coded
        ES["ExpenseService ✅"]:::coded
        FS["FriendshipService ✅"]:::coded
        MS["MailService ✅"]:::coded
        GVS["GroupViewService ✅"]:::coded
    end

    subgraph REPO ["🗄️ Repository Layer"]
        GR["GroupRepository ✅"]:::coded
        GMR["GroupMemberRepository ✅"]:::coded
        UR["UserRepository ✅"]:::coded
        BR["BillRepository ✅"]:::coded
        ER["ExpenseSplitRepository ✅"]:::coded
        FR["FriendshipRepository ✅"]:::coded
        SR["SettlementRepository ✅"]:::coded
    end

    subgraph ENTITY ["📦 Entity Layer"]
        U["User ✅"]:::coded
        G["Group ✅"]:::coded
        GM["GroupMember ✅"]:::coded
        B["Bill ✅"]:::coded
        EX["ExpenseSplit ✅"]:::coded
        F["Friendship ✅"]:::coded
        S["Settlement ✅"]:::coded
    end

    AC --> US
    AC --> MS
    GC --> GS
    GC --> GVS
    UC --> US
    EC --> ES
    EC --> GS
    EC --> GVS
    FC --> FS
    FC --> US
    
    GVS --> GS
    GVS --> ES
    GVS --> GMR
    GVS --> FS

    GS --> GR
    GS --> GMR
    GS --> UR
    GS --> BR
    GS --> ER
    GS --> SR
    GS --> FS
    
    US --> UR
    
    ES --> BR
    ES --> ER
    ES --> GMR
    ES --> SR
    
    FS --> FR
    FS --> UR

    GR -.->|returns| G
    GMR -.->|returns| GM
    UR -.->|returns| U
    BR -.->|returns| B
    ER -.->|returns| EX
    FR -.->|returns| F
    SR -.->|returns| S
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
        - String verificationToken
        - boolean enabled
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
        - Instant createdAt
        - String participantNames
        - String status
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
        - Long id
        - Long userIdA
        - Long userIdB
        - String status
    }
    
    class Settlement {
        - Long id
        - Long groupId
        - Long debtorId
        - Long creditorId
        - BigDecimal amount
        - String status
        - Long confirmedById
        - Instant createdAt
    }

    User "1" --o "n" GroupMember : joins via
    Group "1" --o "n" GroupMember : has
    Group "1" --> "0..*" Bill : contains
    Group "1" --> "0..*" ExpenseSplit : tracked via
    Group "1" --> "0..*" Settlement : has
    User "1" --> "0..*" ExpenseSplit : has per group
    User "1" --> "0..*" Friendship : has
    User "1" --> "0..*" Settlement : involved in
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
    
    class MailService {
        <<Service>>
        + sendVerificationEmail(toEmail String, username String, token String) void
    }

    class GroupService {
        <<Service>>
        - GroupRepository groupRepository
        - GroupMemberRepository groupMemberRepository
        - UserRepository userRepository
        - BillRepository billRepository
        - ExpenseSplitRepository expenseSplitRepository
        - SettlementRepository settlementRepository
        - FriendshipService friendshipService
        + createGroup(name, adminId, memIds) Group
        + findGroupById(groupId) Optional~Group~
        + addMember(groupId, userId, requesterId) void
        + removeMember(groupId, userId) void
        + getGroupsForUser(userId) List~Group~
        + getMembersOfGroup(groupId) List~User~
        + deleteGroup(groupId, requestedId) void
    }

    class ExpenseService {
        <<Service>>
        - BillRepository billRepository
        - ExpenseSplitRepository expenseSplitRepository
        - GroupMemberRepository groupMemberRepository
        - SettlementRepository settlementRepository
        + createEqualBill(request BillCreateRequest) Bill
        + revertSettlement(settlementId Long, byUserId Long) Settlement
        + getActivity(groupId Long) List~Settlement~
        + getSimplifiedDebts(groupId) List~SimplifiedDebtDTO~
        + getBillsForGroup(groupId) List~Bill~
    }
    
    class GroupViewService {
        <<Service>>
        - GroupService groupService
        - ExpenseService expenseService
        - GroupMemberRepository groupMemberRepository
        - FriendshipService friendshipService
        + build(groupId Long, requesterId Long) Map~String, Object~
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
    
    class SettlementRepository {
        <<Repository>>
        + findByGroupId(groupId Long) List~Settlement~
        + save(settlement Settlement) Settlement
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
    GroupService --> BillRepository
    GroupService --> ExpenseSplitRepository
    GroupService --> SettlementRepository
    GroupService --> FriendshipService
    ExpenseService --> BillRepository
    ExpenseService --> ExpenseSplitRepository
    ExpenseService --> GroupMemberRepository
    ExpenseService --> SettlementRepository
    FriendshipService --> FriendshipRepository
    FriendshipService --> UserRepository
    GroupViewService --> GroupService
    GroupViewService --> ExpenseService
    GroupViewService --> GroupMemberRepository
    GroupViewService --> FriendshipService
```

---

## Layer 4: Controller Endpoints

```mermaid
classDiagram
    direction LR
    
    class AuthController {
        <<Controller - Thymeleaf>>
        - UserService userService
        - MailService mailService
        + GET /auth/register
        + POST /auth/register
        + GET /auth/verify
    }

    class UserController {
        <<Controller - Thymeleaf/HTMX>>
        - UserService userService
        + GET /users/search userId Long
    }

    class GroupController {
        <<Controller - Thymeleaf/HTMX>>
        - GroupService groupService
        - GroupViewService groupViewService
        + GET /groups/new
        + POST /groups
        + GET /groups
        + POST /groups/groupId/members userId
        + DELETE /groups/groupId
        + GET /groups/groupId
    }

    class ExpenseController {
        <<RestController - JSON API>>
        - ExpenseService expenseService
        - GroupService groupService
        - GroupViewService groupViewService
        + GET /api/expenses/group/groupId/bills
        + GET /api/expenses/group/groupId/simplified-debts
        + POST /api/expenses/bill
        + POST /api/expenses/settle
    }

    class FriendshipController {
        <<Controller - HTMX/REST>>
        - FriendshipService friendshipService
        - UserService userService
        + GET /api/friends
        + GET /api/friends/pending
        + POST /api/friends/request targetId
        + POST /api/friends/accept friendshipId
    }

    class ViewController {
        <<Controller - Thymeleaf>>
        + GET /
        + GET /dashboard
    }

    AuthController --> UserService
    AuthController --> MailService
    UserController --> UserService
    GroupController --> GroupService
    GroupController --> GroupViewService
    ExpenseController --> ExpenseService
    ExpenseController --> GroupService
    ExpenseController --> GroupViewService
    FriendshipController --> FriendshipService
    FriendshipController --> UserService
```

---

## Layer 5: DTOs (Data Transfer Objects)

```mermaid
classDiagram
    direction LR
    
    class RegisterRequest {
        - String email
        - String username
        - String password
    }

    class BillCreateRequest {
        - Long groupId
        - Long creatorId
        - String description
        - BigDecimal totalAmount
        - List~Long~ participantIds
        - String participantNames
    }
    
    class DetailedBillRequest {
        - Long groupId
        - String description
        - BigDecimal totalAmount
        - List~SplitLine~ splits
        - Long userId
        - BigDecimal amountOwed
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

    class BalanceCalc {
        <<Internal DTO - OSIV Safe>>
        - Long userId
        - BigDecimal balance
    }

    AuthController ..> RegisterRequest : receives
    ExpenseController ..> BillCreateRequest : receives
    ExpenseController ..> DetailedBillRequest : receives
    ExpenseController ..> SettleUpRequest : receives
    ExpenseController ..> SimplifiedDebtDTO : returns
```