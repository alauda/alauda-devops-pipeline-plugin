package com.alauda.jenkins.plugins.storage;

import org.junit.Test;

import java.awt.*;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class StorageTest {

    @Test
    public void paramTest() {
        IParam p1 = new Param(10, "label1", "label2");
        IParam p2 = new Param("some words");
        p2.addLabels();
        p1.addLabels("label3", "label4");
        assertEquals(p1.getValue(), 10);
        assertEquals(p2.getValue(), "some words");
        assertEquals(p1.hasPartialLabels("label1", "label6"), true);
        assertEquals(p1.hasPartialLabels("label5", "label6"), false);
        assertEquals(p1.hasAllLabels("label1", "label2", "label3"), true);
        assertEquals(p1.hasAllLabels("label1", "label2", "label5"), false);

    }

    @Test
    public void storageTest() {
        IStorage storage = new Storage();
        storage.add("build.git", "https://x.git", "build", "notify");
        storage.add("build.tag", 1.35, "build");
        storage.add("build.usr", "admin", "build");
        storage.add("server.name", "app1", "server");
        storage.add("server.time", 13526157851L, "server", "notify");
        storage.addLabel("build.tag", "notify");
        storage.addLabel("build.key", "notify");

        assertEquals(storage.getObject("build.tag"), 1.35);
        assertEquals(storage.getObject("server.name"), "app1");
        assertEquals(storage.getObject("unknown.key", 1), 1);

        Map map1 = storage.getByAllLabels("build", "notify");
        assertEquals(map1.size(), 2);
        assertEquals(map1.get("build.git"), "https://x.git");

        Map map2 = storage.getByPartialLabels("build", "notify");
        assertEquals(map2.size(), 4);
        assertEquals(map2.containsKey("server.name"), false);

    }
}
