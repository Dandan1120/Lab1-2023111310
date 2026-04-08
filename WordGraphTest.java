import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

public class WordGraphTest {

    @Before
    public void setUp() {
        // 每次执行测试前，初始化清空图
        WordGraph.graph.clear();

        // 你的专属测试文本
        String text = "The scientist carefully analyzed the data, wrote a detailed report, and shared the report with the team, but the team requested more data, so the scientist analyzed it again.";

        // 模拟 Lab1 中清洗文本和建图的过程
        String[] words = text.toLowerCase().replaceAll("[^a-z ]", " ").trim().split("\\s+");

        // 动态构建有向图
        for (int i = 0; i < words.length - 1; i++) {
            String u = words[i];
            String v = words[i + 1];
            WordGraph.graph.putIfAbsent(u, new HashMap<>());
            WordGraph.graph.get(u).put(v, WordGraph.graph.get(u).getOrDefault(v, 0) + 1);
        }
        // 确保最后一个单词也在图的 key 中
        WordGraph.graph.putIfAbsent(words[words.length - 1], new HashMap<>());
    }

    @Test
    public void testQueryBridgeWords_Case1_HasBridge() {
        // 用例 1：均存在，且有桥接词 (shared -> the -> report)
        String expected = "The bridge words from \"shared\" to \"report\" are: the.";
        String actual = WordGraph.queryBridgeWords("shared", "report");
        assertEquals(expected, actual);
    }

    @Test
    public void testQueryBridgeWords_Case2_NoBridge() {
        // 用例 2：均存在，但无桥接词
        String expected = "No bridge words from \"scientist\" to \"team\"!";
        String actual = WordGraph.queryBridgeWords("scientist", "team");
        assertEquals(expected, actual);
    }

    @Test
    public void testQueryBridgeWords_Case3_Word1Missing() {
        // 用例 3：word1 不存在 ("doctor")，word2 存在 ("data")
        String expected = "No \"doctor\" in the graph!";
        String actual = WordGraph.queryBridgeWords("doctor", "data");
        assertEquals(expected, actual);
    }

    @Test
    public void testQueryBridgeWords_Case4_Word2Missing() {
        // 用例 4：word1 存在 ("report")，word2 不存在 ("doctor")
        String expected = "No \"doctor\" in the graph!";
        String actual = WordGraph.queryBridgeWords("report", "doctor");
        assertEquals(expected, actual);
    }

    @Test
    public void testQueryBridgeWords_Case5_BothMissing() {
        // 用例 5：word1 和 word2 均不存在 ("doctor", "nurse")
        String expected = "No \"doctor\" and \"nurse\" in the graph!";
        String actual = WordGraph.queryBridgeWords("doctor", "nurse");
        assertEquals(expected, actual);
    }
}