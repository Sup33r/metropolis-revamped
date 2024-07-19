# Metropolis Revamped

# This is kind of abandoned, but its something atleast. Might, or might not work on it later.

## A plugin for city management, with striking similarities to towny

## Permissions
- metropolis.city.claim
- metropolis.city.new
- metropolis.city.bank
- metropolis.city.set.motd
- metropolis.city.set.enter
- metropolis.city.set.exit
- metropolis.homecity
- metropolis.city.go
- metropolis.city.go.delete
- metropolis.city.go.set.name
- metropolis.city.go.set.accesslevel
- metropolis.city.go.info

## Setup
You need:
- The Vault plugin
- CoreProtect plugin
- A MySQL DB

### Hot to setup DB
To setup the DB you need to have a user which will be able to access a specified database. Then enter in the database:s name into the config.yml file.
sql:
  host: YOUR_HOST
  port: YOUR_PORT (NORMALLY 3306)
  database: YOUR_DB_NAME
  username: YOUR_USERNAME
  password: YOUR_PASSWORD

