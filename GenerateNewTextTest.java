
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

public class GenerateNewTextTest {

    @Before
    public void setUp() {
        // 每次测试前清空并重新构造图
        WordGraph.graph.clear();

        // 构造节点 apple，并添加出边 apple -> banana
        WordGraph.graph.put("apple", new HashMap<>());
        WordGraph.graph.get("apple").put("banana", 1);

        // 构造节点 banana，并添加出边 banana -> cat
        WordGraph.graph.put("banana", new HashMap<>());
        WordGraph.graph.get("banana").put("cat", 1);

        // 构造节点 pear，没有出边 (用于测试 N7 不满足的情况)
        WordGraph.graph.put("pear", new HashMap<>());

        // 构造节点 cat，没有出边
        WordGraph.graph.put("cat", new HashMap<>());
    }

    @Test
    public void testGenerateNewText_Path1_SingleWord() {
        // 用例 1：输入单词数 < 2 (覆盖路径 1)
        String expected = "apple";
        String actual = WordGraph.generateNewText("apple");
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateNewText_Path3_W1NotInGraph() {
        // 用例 2：w1("dog") 不在图中 (覆盖路径 3)
        String expected = "dog cat";
        String actual = WordGraph.generateNewText("dog cat");
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateNewText_Path4_W1NoEdges() {
        // 用例 3：w1("pear") 在图中，但没有出边，不进入内层循环 (覆盖路径 4)
        String expected = "pear cat";
        String actual = WordGraph.generateNewText("pear cat");
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateNewText_Path5_BridgeNotConnectW2() {
        // 用例 4：进入内层循环，但 bridge("banana") 没有连向 w2("dog") 的边 (覆盖路径 5)
        String expected = "apple dog";
        String actual = WordGraph.generateNewText("apple dog");
        assertEquals(expected, actual);
    }

    @Test
    public void testGenerateNewText_Path6_HasBridge() {
        // 用例 5：成功找到桥接词 "banana"，拼接成新文本 (覆盖路径 6/7)
        String expected = "apple banana cat";
        String actual = WordGraph.generateNewText("apple cat");
        assertEquals(expected, actual);
    }
}