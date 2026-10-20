GI-3: authenticatie en rollen. Deze package maakt en controleert JWT-tokens (`JwtConfig`, `JwtSettings`), hasht wachtwoorden met Argon2id (`PasswordHasher`), levert de rolcheck voor routes (`requireRole`) en de endpoints `/auth/register` en `/auth/login`. Er wordt niets versleuteld: wachtwoorden worden gehasht en tokens ondertekend (§16.1).

Eigenaar: Lonneke (gedeeld onderdeel)
GI-3 Authenticatie en rollen
