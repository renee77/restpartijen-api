Persistence is responsible for storing and retrieving data in an application. It covers the mechanisms and strategies used to store data in databases, files or other storage media, and to retrieve that data later for use in the application.

A table that another feature refers to lives here (issue #36). Every feature may import `persistence`, so every feature can put a foreign key on that table. The owner of the table stays the same (§9.2); only its location changes. This package now holds `UsersTable` (Lonneke), `ProductsTable` and `SuppliersTable` (Eva).

Owner: Shared: Eva
Part: GI-1 Persistence layer
