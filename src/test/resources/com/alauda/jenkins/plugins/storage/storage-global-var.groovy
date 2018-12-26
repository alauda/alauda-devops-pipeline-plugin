//pipeline {
////    agent any
////
////    stages{
////        stage("one"){
////            steps {
////                echo "hello"
////
////                alaudaStorage.add("key", "value", "label")
////            }
////        }
////        stage("two"){
////            steps {
////                echo "hello"
////
////                echo alaudaStorage.get("key")
////            }
////        }
////    }
////}

alaudaStorage.add("key", "value", "label")
echo alaudaStorage.getObject("key")

echo alaudaStorage.getObject("noop", "defaultValue")

alaudaStorage.addLabel("key", "newLabel")

println alaudaStorage.getByPartialLabels("newLabel")