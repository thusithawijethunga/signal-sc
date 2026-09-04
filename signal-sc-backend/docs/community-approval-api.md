# Community Post Approval - Mobile API Documentation

**Base URL:** `https://your-domain.com/api`

**Authentication:** Bearer token or `X-Api-Token` header

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Post Lifecycle](#post-lifecycle)
4. [Comment Lifecycle](#comment-lifecycle)
5. [User Endpoints](#user-endpoints)
6. [Admin Moderation Endpoints](#admin-moderation-endpoints)
7. [Data Models](#data-models)
8. [Error Responses](#error-responses)

---

## Overview

The Community Post Approval system allows admin users to moderate community posts and comments. When approval is enabled, posts/comments enter a `pending` state and must be approved or rejected by an admin before becoming visible to other users.

**Flow:**
```
User creates post → Status: "pending" → Admin approves → Status: "approved" (visible to all)
                                                 → Admin rejects → Status: "rejected" (visible to author)
```

**Settings** (controlled via `community/settings` endpoint):
- `require_post_approval` (default: `true`) — posts require admin approval
- `require_comment_approval` (default: `false`) — comments require admin approval

---

## Authentication

All API requests require authentication via:

```
Authorization: Bearer <token>
```

or

```
X-Api-Token: <token>
```

**Login** to obtain a token:
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password"
}
```

**Response:**
```json
{
  "user": { "id": 1, "name": "John", "email": "john@example.com", "role": "admin" },
  "token": "abc123..."
}
```

> Admin endpoints require `role: "admin"`. Regular users get `role: "viewer"`.

---

## Post Lifecycle

### States

| State | Description |
|-------|-------------|
| `pending` | Awaiting admin approval (not visible to other users) |
| `approved` | Published and visible to all users |
| `rejected` | Rejected by admin (visible only to author) |

### User View Logic

- **Non-admin users** see: all `approved` posts + their own `pending` and `rejected` posts
- **Admin users** see: all posts with optional `status` filter

---

## Comment Lifecycle

### States

| State | Description |
|-------|-------------|
| `pending` | Awaiting admin approval |
| `approved` | Published and visible to all |

> Rejected comments are deleted, not marked as rejected.

---

## User Endpoints

### GET /api/community/posts

Get community posts. Non-admins see approved posts + own pending/rejected posts.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `post_type` | string | No | Filter: `text`, `trade_update`, `screenshot`, `signal_card` |
| `pair` | string | No | Filter by trading pair |
| `search` | string | No | Search content, author name, hashtags |
| `status` | string | No | Filter by status (admin only) |
| `per_page` | int | No | Results per page (default: 20) |

**Response:** `200 OK`
```json
{
  "current_page": 1,
  "data": [
    {
      "id": 1,
      "user_id": 1,
      "author_name": "John",
      "author_badge": "VIP",
      "author_avatar_hex": 12345,
      "post_type": "text",
      "content": "Great trade setup!",
      "hashtags": "#forex #GBPUSD",
      "image_uri": null,
      "pair": "GBPUSD",
      "trade_type": null,
      "entry_price": null,
      "exit_price": null,
      "lot_size": null,
      "profit_amount": null,
      "pips_gain": null,
      "roi_percentage": null,
      "broker_name": null,
      "card_theme": null,
      "is_verified_trade": false,
      "likes_count": 5,
      "fire_count": 2,
      "rocket_count": 1,
      "comments_count": 3,
      "is_pinned": false,
      "status": "approved",
      "rejection_reason": null,
      "comments_need_review": false,
      "user": { "id": 1, "name": "John", "email": "john@example.com", "role": "admin" }
    }
  ],
  "last_page": 5,
  "per_page": 20,
  "total": 100
}
```

---

### POST /api/community/posts

Create a new post. If approval is required, status will be `pending`.

**Request Body:**
```json
{
  "content": "Great trade setup on GBPUSD!",
  "post_type": "text",
  "hashtags": "#forex #GBPUSD",
  "image_uri": null,
  "pair": "GBPUSD",
  "trade_type": "buy",
  "entry_price": "1.2500",
  "exit_price": "1.2600",
  "lot_size": "0.10",
  "profit_amount": 100.00,
  "pips_gain": 100,
  "roi_percentage": 10.0,
  "broker_name": "XM",
  "card_theme": "dark",
  "author_badge": "VIP",
  "author_avatar_hex": 12345
}
```

**Required Fields:**
| Field | Type | Description |
|-------|------|-------------|
| `content` | string | Post content |
| `post_type` | string | `text`, `trade_update`, `screenshot`, `signal_card` |

**Response:** `201 Created`
```json
{
  "post": {
    "id": 1,
    "status": "pending",
    "content": "Great trade setup on GBPUSD!",
    ...
  },
  "message": "Post submitted for review. It will appear after admin approval.",
  "status": "pending"
}
```

---

### DELETE /api/community/posts/{id}

Delete a post. Users can only delete their own posts; admins can delete any.

**Response:** `200 OK`
```json
{
  "message": "Post deleted"
}
```

---

### GET /api/community/posts/{id}/comments

Get comments for a post.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `per_page` | int | No | Results per page (default: 50) |

**Response:** `200 OK`
```json
{
  "current_page": 1,
  "data": [
    {
      "id": 1,
      "post_id": 1,
      "user_id": 2,
      "author_name": "Jane",
      "content": "Nice setup!",
      "created_at": "2026-09-04T10:00:00Z",
      "likes_count": 0,
      "status": "approved",
      "user": { "id": 2, "name": "Jane", "role": "viewer" }
    }
  ],
  "last_page": 1,
  "per_page": 50,
  "total": 1
}
```

---

### POST /api/community/posts/{id}/comments

Create a comment. If approval is required, status will be `pending`.

**Request Body:**
```json
{
  "content": "Nice setup!"
}
```

**Response:** `201 Created`
```json
{
  "comment": {
    "id": 1,
    "post_id": 1,
    "content": "Nice setup!",
    "status": "pending",
    ...
  },
  "message": "Comment submitted for review.",
  "status": "pending"
}
```

---

### POST /api/community/posts/{id}/react

React to a post with an emoji.

**Request Body:**
```json
{
  "emoji": "thumbs"
}
```

**Valid emojis:** `thumbs`, `fire`, `rocket`

**Response:** `200 OK`
```json
{
  "message": "Reaction added",
  "post": { ... }
}
```

---

### GET /api/community/settings

Get community settings (admin only).

**Response:** `200 OK`
```json
{
  "require_post_approval": true,
  "require_comment_approval": false,
  "max_post_length": "5000"
}
```

---

## Admin Moderation Endpoints

> All admin endpoints require `role: "admin"`.

### GET /api/admin/community/stats

Get moderation statistics.

**Response:** `200 OK`
```json
{
  "pending_posts": 5,
  "approved_posts": 120,
  "rejected_posts": 3,
  "pending_comments": 2,
  "approved_comments": 450,
  "total_posts": 128,
  "total_comments": 452
}
```

---

### GET /api/admin/community/pending-posts

Get all pending posts awaiting approval.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `per_page` | int | No | Results per page (default: 20) |

**Response:** `200 OK`
```json
{
  "current_page": 1,
  "data": [
    {
      "id": 5,
      "user_id": 3,
      "author_name": "Trader123",
      "post_type": "text",
      "content": "Check out this setup...",
      "status": "pending",
      "created_at": "2026-09-04T09:00:00Z",
      "user": { "id": 3, "name": "Trader123", "role": "viewer" }
    }
  ],
  "last_page": 1,
  "per_page": 20,
  "total": 1
}
```

---

### GET /api/admin/community/posts

Get all posts with optional status filter.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | string | No | Filter: `pending`, `approved`, `rejected` |
| `per_page` | int | No | Results per page (default: 20) |

**Response:** `200 OK` (same structure as pending-posts)

---

### POST /api/admin/community/posts/{id}/approve

Approve a pending post and broadcast it via WebSocket.

**Response:** `200 OK`
```json
{
  "message": "Post approved and published",
  "post": {
    "id": 5,
    "status": "approved",
    "approved_at": "2026-09-04T10:30:00Z",
    "approved_by": 1,
    "rejection_reason": null,
    ...
  }
}
```

---

### POST /api/admin/community/posts/approve-all

Approve all pending posts at once.

**Response:** `200 OK`
```json
{
  "message": "5 posts approved and published",
  "count": 5
}
```

---

### POST /api/admin/community/posts/{id}/reject

Reject a pending post with optional reason.

**Request Body:**
```json
{
  "rejection_reason": "Does not meet community guidelines"
}
```

**Response:** `200 OK`
```json
{
  "message": "Post rejected",
  "post": {
    "id": 5,
    "status": "rejected",
    "rejection_reason": "Does not meet community guidelines",
    "approved_by": 1,
    ...
  }
}
```

---

### DELETE /api/admin/community/posts/{id}

Delete any post (admin only).

**Response:** `200 OK`
```json
{
  "message": "Post deleted"
}
```

---

### GET /api/admin/community/pending-comments

Get all pending comments awaiting approval.

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `per_page` | int | No | Results per page (default: 20) |

**Response:** `200 OK`
```json
{
  "current_page": 1,
  "data": [
    {
      "id": 10,
      "post_id": 5,
      "user_id": 4,
      "author_name": "Trader456",
      "content": "Great analysis!",
      "status": "pending",
      "post": { "id": 5, "content": "Check out this setup..." },
      "user": { "id": 4, "name": "Trader456", "role": "viewer" }
    }
  ],
  "last_page": 1,
  "per_page": 20,
  "total": 1
}
```

---

### POST /api/admin/community/comments/{id}/approve

Approve a comment and increment the parent post's `comments_count`.

**Response:** `200 OK`
```json
{
  "message": "Comment approved",
  "comment": {
    "id": 10,
    "status": "approved",
    "approved_at": "2026-09-04T10:30:00Z",
    "approved_by": 1,
    ...
  }
}
```

---

### POST /api/admin/community/comments/approve-all

Approve all pending comments at once.

**Response:** `200 OK`
```json
{
  "message": "3 comments approved",
  "count": 3
}
```

---

### POST /api/admin/community/comments/{id}/reject

Reject and delete a comment.

**Response:** `200 OK`
```json
{
  "message": "Comment rejected and deleted"
}
```

---

## Data Models

### CommunityPost

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Post ID |
| `user_id` | int | Author user ID |
| `author_name` | string | Author display name |
| `author_badge` | string? | Badge text (e.g., "VIP") |
| `author_avatar_hex` | long? | Avatar color hex |
| `post_type` | string | `text`, `trade_update`, `screenshot`, `signal_card` |
| `content` | string | Post content |
| `hashtags` | string? | Hashtags string |
| `image_uri` | string? | Image URL |
| `pair` | string? | Trading pair |
| `trade_type` | string? | `buy` or `sell` |
| `entry_price` | decimal? | Entry price |
| `exit_price` | decimal? | Exit price |
| `lot_size` | decimal? | Lot size |
| `profit_amount` | decimal? | Profit amount |
| `pips_gain` | decimal? | Pips gained |
| `roi_percentage` | decimal? | ROI percentage |
| `broker_name` | string? | Broker name |
| `card_theme` | string? | Card theme |
| `is_verified_trade` | boolean | Whether trade is verified |
| `likes_count` | int | Thumbs-up reactions count |
| `fire_count` | int | Fire reactions count |
| `rocket_count` | int | Rocket reactions count |
| `comments_count` | int | Approved comments count |
| `is_pinned` | boolean | Whether post is pinned |
| `status` | string | `pending`, `approved`, `rejected` |
| `rejection_reason` | string? | Rejection reason (if rejected) |
| `comments_need_review` | boolean | Whether comments need approval |
| `approved_at` | datetime? | Approval timestamp |
| `approved_by` | int? | Admin user ID who approved |
| `created_at` | datetime | Creation timestamp |
| `updated_at` | datetime | Last update timestamp |

### CommunityComment

| Field | Type | Description |
|-------|------|-------------|
| `id` | int | Comment ID |
| `post_id` | int | Parent post ID |
| `user_id` | int | Author user ID |
| `author_name` | string | Author display name |
| `content` | string | Comment content |
| `likes_count` | int | Likes count |
| `status` | string | `pending`, `approved` |
| `approved_at` | datetime? | Approval timestamp |
| `approved_by` | int? | Admin user ID who approved |
| `created_at` | datetime | Creation timestamp |

---

## Error Responses

### 401 Unauthorized
```json
{
  "message": "Unauthorized"
}
```

### 403 Forbidden
```json
{
  "message": "Unauthorized"
}
```

### 422 Validation Error
```json
{
  "message": "Validation failed",
  "errors": {
    "content": ["The content field is required."],
    "post_type": ["The post_type field is required."]
  }
}
```

### 500 Server Error
```json
{
  "message": "Failed to fetch posts",
  "error": "Error details..."
}
```
