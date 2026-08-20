# A server emulator

## Functional stuff

### Ingame Commands
See `AdminCommands` for a list of commands you can do ingame. Typing `!help` will show you all possible commands 
(without parameters). Most commands will display expected parameters if you try use them without parameters.

## Technical stuff
How to install: see #server-setup-guide in the Swordie discord.

* Java (21)
* MySQL
 * Custom database migrations (see `MigrationManager`)
* Half-custom REST layer using RESTEasy (see `WebApi`)
* Custom API using raw bytes, used before we had the REST layer (see `ApiAcceptor`)
* Manual SQL
  * If you find JPA/Hibernate stuff it's not actually used.
  * We switched off of Hibernate when it was causing issues in prod and it being hard to debug
* Netty for networking

Build with `Build ComfyStory Server.cmd` and run with `Start ComfyStory Server.cmd`. Both scripts select Java 21 only
for their own process, so the user-level `JAVA_HOME` can remain configured for other servers. Set
`COMFYSTORY_JAVA_HOME` if Java 21 is installed outside the Eclipse Adoptium directory under Program Files.

Feel free to create PRs to improve this source. You can create a fork of this repository, then work on that and 
create a merge request towards this main repository. Tag Asura or Sjonnie on discord to ask for a review.

## Links & Community Resources
* [RaGEZONE Release Post (contains setup guide and client files)](https://forum.ragezone.com/threads/xmas-release-v232-swordie-source.1257894/)
* [AuthHook & Launcher](https://forum.ragezone.com/threads/swordie-v232-launcher-friends-family-use-only.1258180/)
