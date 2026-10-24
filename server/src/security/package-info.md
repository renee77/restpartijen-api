GI-3: authentication and roles. This package creates and verifies JWT tokens (`JwtConfig`, `JwtSettings`), hashes passwords with Argon2id (`PasswordHasher`), provides the role check for routes (`requireRole`) and the endpoints `/auth/register` and `/auth/login`. Nothing is encrypted: passwords are hashed and tokens are signed (§16.1).

Owner: Lonneke (shared part)
GI-3 Authentication and roles
