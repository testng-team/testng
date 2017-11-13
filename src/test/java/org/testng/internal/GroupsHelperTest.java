package org.testng.internal;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GroupsHelperTest {

    @Test
    public void testCreateGroupsWithoutMetaGroups() {
        Map<String, String> expected = Maps.newHashMap();
        expected.put("foo", "foo");
        Map<String, String> actual = GroupsHelper.createGroups(Collections.<String, List<String>>emptyMap(), Collections.singletonList("foo")
        );
        assertThat(actual).containsAllEntriesOf(expected);
    }

    @Test(dataProvider = "getTestData")
    public void testCreateGroupsWithMetaGroups(String metaGrpName, List<String> grpName, Map<String, String> expected) {
        Map<String, List<String>> metaGroups = Maps.newHashMap();
        metaGroups.put(metaGrpName, Collections.singletonList(metaGrpName));
        Map<String, String> actual = GroupsHelper.createGroups(metaGroups, grpName);
        assertThat(actual).containsAllEntriesOf(expected);
    }

    @DataProvider(name = "getTestData")
    public Object[][] getTestData() {
        return new Object[][]{
                {"bar", Collections.singletonList("foo"), constructExpectedMap("foo")},
                {"bar", Arrays.asList("foo", "bar"), constructExpectedMap("foo", "bar")}
        };
    }

    private static Map<String, String> constructExpectedMap(String... keys) {
        Map<String, String> map = Maps.newHashMap();
        for (String key : keys) {
            map.put(key, key);
        }
        return map;
    }

}
