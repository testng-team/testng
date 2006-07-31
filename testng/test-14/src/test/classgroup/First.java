package test.classgroup;



/**
 * @testng.test groups="first"
 */
public class First {
    private static boolean m_first1 = false;
    private static boolean m_first2 = false;

    /**
     * @tesng.test
     */
    public void first1() {
        m_first1 = true;
    }

    /**
     * @tesng.test
     */
    public void first2() {
        m_first2 = true;
    }

    static boolean allRun() {
        return m_first1 && m_first2;
    }
}