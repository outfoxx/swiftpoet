Releasing
=========

Cutting a Release
---------------------

1. Update `CHANGELOG.md`.

2. Set versions:

    ```
    export RELEASE_VERSION=X.Y.Z
    export NEXT_VERSION=X.Y.Z-SNAPSHOT
    ```

3. Update versions:

    ```
    sed -i "" \
      "s/releaseVersion=.*/releaseVersion=$RELEASE_VERSION/g" \
      `find . -name "gradle.properties"`
    sed -i "" \
      "s/'io.outfoxx:\([^\:]*\):[^']*'/'io.outfoxx:\1:$RELEASE_VERSION'/g" \
      `find . -name "README.md"`
    sed -i "" \
      "s/outfoxx.github.io\/swiftpoet\/[^\/]*\/\(.*\)/outfoxx.github.io\/swiftpoet\/$RELEASE_VERSION\/\1/g" \
      `find . -name "README.md"`
    sed -i "" \
      "s/\<version\>\([^<]*\)\<\/version\>/\<version\>$RELEASE_VERSION\<\/version\>/g" \
      `find . -name "README.md"`
    ```

4. Tag the release and push to GitHub.

    ```
    git commit -am "Prepare for release $RELEASE_VERSION."
    git tag -a $RELEASE_VERSION -m "Version $RELEASE_VERSION"
    git push && git push --tags
    ```

5. Wait for [GitHub Actions][github_actions] to start building the release.

6. Prepare for ongoing development and push to GitHub.

    ```
    sed -i "" \
      "s/releaseVersion=.*/releaseVersion=$NEXT_VERSION/g" \
      `find . -name "gradle.properties"`
    git commit -am "Prepare next development version."
    git push
    ```

7. CI will release the artifact and publish the documentation.

 [sonatype_issues]: https://issues.sonatype.org/
 [sonatype_nexus]: https://oss.sonatype.org/
 [github_actions]: https://github.com/outfoxx/swiftpoet/actions