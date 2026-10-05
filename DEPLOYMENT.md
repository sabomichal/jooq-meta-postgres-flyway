# Deployment Instructions

Artifacts are published to Maven Central with the `central-publishing-maven-plugin` (server id `central` in `~/.m2/settings.xml`, auto-publish enabled).
Artifacts are signed by the `sign` profile, which is active whenever the `gpg.passphrase` property is set.

Release `X.Y.Z`:
1. Point README at the new version, commit `docs: point README at X.Y.Z`.
2. Set `<version>X.Y.Z</version>` in `pom.xml`, commit `release version X.Y.Z`, tag `vX.Y.Z`.
3. `mvn -B clean deploy`
4. Set `<version>0-SNAPSHOT</version>` back, commit `next development version`.
5. Push `master` and the tag, create the GitHub release from the tag.
