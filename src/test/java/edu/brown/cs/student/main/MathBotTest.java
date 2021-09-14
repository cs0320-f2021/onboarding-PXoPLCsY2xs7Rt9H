package edu.brown.cs.student.main;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MathBotTest {

  @Test
  public void testAddition() {
    MathBot matherator9000 = new MathBot();
    double output = matherator9000.add(10.5, 3);
    assertEquals(13.5, output, 0.01);
  }

  @Test
  public void testLargerNumbers() {
    MathBot matherator9001 = new MathBot();
    double output = matherator9001.add(100000, 200303);
    assertEquals(300303, output, 0.01);
  }

  @Test
  public void testSubtraction() {
    MathBot matherator9002 = new MathBot();
    double output = matherator9002.subtract(18, 17);
    assertEquals(1, output, 0.01);
  }

  // TODO: add more unit tests of your own
  @Test
  public void testSubtractLargerNumbers() {
    MathBot matherator9003 = new MathBot();
    double output = matherator9003.subtract(100000, 200303);
    assertEquals(-100303, output, 0.01);
  }

  @Test
  public void testSubtractZero() {
    MathBot matherator9004 = new MathBot();
    double output = matherator9004.subtract(0, 0);
    assertEquals(0, output, 0.01);
  }

  @Test
  public void testAddZero() {
    MathBot matherator9005 = new MathBot();
    double output = matherator9005.add(5, 0);
    assertEquals(5, output, 0.01);
  }

  @Test
  public void testSmallNumbers() {
    MathBot matherator9006 = new MathBot();
    double output = matherator9006.add(.000001, .000000006);
    assertEquals(.000001006, output, 0.01);
  }
}
