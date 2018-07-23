package test.newinstancepermethod;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.NewInstancePerMethod;
import org.testng.annotations.Test;

import java.util.concurrent.ThreadLocalRandom;

@NewInstancePerMethod
public class ParallelAttributeTest {
        private int member;
        private ThreadLocal<Integer> expected = new ThreadLocal<>();

        @BeforeMethod
        public void setup() {
            member = ThreadLocalRandom.current().nextInt(1, 51);
            expected.set(member);
        }

        @Test
        public void first() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 800));
            Assert.assertEquals(member, expected.get().intValue(), "member variable and thread local do not match");
        }

        @Test
        public void second() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 800));
            Assert.assertEquals(member, expected.get().intValue(), "member variable and thread local do not match");
        }

        @Test
        public void third() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 800));
            Assert.assertEquals(member, expected.get().intValue(), "member variable and thread local do not match");
        }

        @Test
        public void fourth() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 800));
            Assert.assertEquals(member, expected.get().intValue(), "member variable and thread local do not match");
        }

        @Test
        public void fifth() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 800));
            Assert.assertEquals(member, expected.get().intValue(), "member variable and thread local do not match");
        }

        @Test
        public void sixth() throws Exception {
            Thread.sleep(ThreadLocalRandom.current().nextLong(100, 800));
            Assert.assertEquals(member, expected.get().intValue(), "member variable and thread local do not match");
        }
}
