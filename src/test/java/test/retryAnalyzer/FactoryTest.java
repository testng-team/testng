package test.retryAnalyzer;

import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.Test;

public class FactoryTest implements ITest {

  public static int m_count = 0;

  private String name;

	@Override
  public String getTestName() {
		return  this.name;
	}

	public FactoryTest(String name){

		this.name = name;

	}

	@Test(retryAnalyzer = MyRetry.class)
	public void someTest1(){
		System.out.println("Test Called : "+ this.name);
		if(this.name.contains("5")){
//			System.out.println("fail");
			m_count++;
			Assert.fail();
		}else{

		Assert.assertEquals(true, true);
		}
	}





}
