/**
 * This script demonstrates the usage of
 * alaudaDevops.doAs and alaudaDevops.withCredentials
 * This script assumes the following setup:
 *  Environment:
 *      username: userone
 *          project: userone
 *      username: usertwo
 *          project: usertwo
 *  And that the credentials are setup correctly
 *  in Jenkins under the userone account
 */
try {
    timeout(time: 20, unit: 'MINUTES') {
        // Select the default cluster
        echo "Selecting the default cluster"
        alaudaDevops.withCluster() {
            //Select the default user
            echo "Logging in as the default user (userone) using withCredentials"
            alaudaDevops.withCredentials() {
                // Select the default project (userone)
                try {
                    echo "Selecting the default project (userone)"
                    alaudaDevops.withProject() {
                        // Create a nodejs application
                        // This should succeed
                        echo "This should succeed"
                        alaudaDevops.create('https://raw.githubusercontent.com/alaudaDevops/nodejs-ex/master/alaudaDevops/templates/nodejs-mongodb.json')
                    }
                } catch (err) {
                    echo "in catch block"
                    echo "Caught: ${err}"
                }
                // Try creating an application in usertwo
                try {
                    echo "Selecting project (usertwo)"
                    alaudaDevops.withProject('usertwo') {
                        // This should fail
                        echo "This should fail"
                        alaudaDevops.create('https://raw.githubusercontent.com/alaudaDevops/nodejs-ex/master/alaudaDevops/templates/nodejs-mongodb.json')
                    }
                } catch (err) {
                    echo "in catch block"
                    echo "Caught: ${err}"
                }
            }
            // Provide the credentials for usertwo
            echo "Logging in as usertwo using withCredentials"
            alaudaDevops.withCredentials('usertwo') {
                // Create an application in usertwo
                echo "Selecting project usertwo"
                alaudaDevops.withProject('usertwo') {
                    // This should succeed now
                    try {
                        echo "This should succeed now"
                        alaudaDevops.create('https://raw.githubusercontent.com/alaudaDevops/nodejs-ex/master/alaudaDevops/templates/nodejs-mongodb.json')
                    } catch (err) {
                        echo "in catch block"
                        echo "Caught: ${err}"
                    }
                }
            }
            // Switch back to the default user (userone)
            echo "Logging in as user userone using doAs"
            alaudaDevops.doAs('userone') {
                // Select the default project again (userone)
                echo "Selecting project userone"
                alaudaDevops.withProject('userone') {
                    // Create a rails application in userone
                    try {
                        // This should succeed
                        echo "This should succeed"
                        alaudaDevops.create('https://raw.githubusercontent.com/alaudaDevops/rails-ex/master/alaudaDevops/templates/rails-postgresql.json')
                    } catch (err) {
                        echo "in catch block"
                        echo "Caught: ${err}"
                    }
                }
                // Try creating the application in usertwo
                echo "Selecting project usertwo"
                alaudaDevops.withProject('usertwo') {
                    // This should fail
                    try {
                        echo "This should fail"
                        alaudaDevops.create('https://raw.githubusercontent.com/alaudaDevops/rails-ex/master/alaudaDevops/templates/rails-postgresql.json')
                    } catch (err) {
                        echo "in catch block"
                        echo "Caught: ${err}"
                    }
                }
            }
            // Switch back to userTwo using doAs
            echo "Logging in as usertwo using doAs"
            alaudaDevops.doAs('usertwo') {
                // Select usertwo
                echo "Selecting project usertwo"
                alaudaDevops.withProject('usertwo') {
                    // Create a rails application in usertwo
                    try {
                        // This should succeed now
                        echo "This should succeed now"
                        alaudaDevops.create('https://raw.githubusercontent.com/alaudaDevops/rails-ex/master/alaudaDevops/templates/rails-postgresql.json')
                    } catch (err) {
                        echo "in catch block"
                        echo "Caught: ${err}"
                    }
                }
            }
        }
    }
} catch (err) {
    echo "in catch block"
    echo "Caught: ${err}"
}
