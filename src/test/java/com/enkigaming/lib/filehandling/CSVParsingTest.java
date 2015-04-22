package com.enkigaming.lib.filehandling;

import org.junit.Test;
import com.enkigaming.lib.testing.ThrowableAssertion;
import static org.junit.Assert.*;
import static com.enkigaming.lib.testing.Assert.*;
import java.util.Arrays;
import java.util.List;

public class CSVParsingTest
{
    @Test
    public void testSplitCSVLine()
    {
        assertListEquals("1", CSVFileHandler.splitCSVLine("Blue, Black, Red, Green, White, Purple, Yellow"),
                              Arrays.asList("Blue", "Black", "Red", "Green", "White", "Purple", "Yellow"));
        
        assertListEquals("2", CSVFileHandler.splitCSVLine("Blue, Black, \"Red\", Green, \"White\", Purple, Yellow"),
                              Arrays.asList("Blue", "Black", "Red", "Green", "White", "Purple", "Yellow"));
        
        assertListEquals("3", CSVFileHandler.splitCSVLine("Blue, \"Black, Red\", Green, \"White\", \"Purple, Yellow\""),
                              Arrays.asList("Blue", "Black, Red", "Green", "White", "Purple, Yellow"));
        
        assertListEquals("4", CSVFileHandler.splitCSVLine("Blue, Black(Red, Green), \"White\", \"Purple, Yellow\""),
                              Arrays.asList("Blue", "Black(Red, Green)", "White", "Purple, Yellow"));
        
        assertListEquals("5", CSVFileHandler.splitCSVLine("Blue, \"\"\"Black, Red\"\", Green\", White, Purple, Yellow"),
                              Arrays.asList("Blue", "\"Black, Red\", Green", "White", "Purple", "Yellow"));
        
        assertListEquals("6", CSVFileHandler.splitCSVLine("Blue, \"\\\"Black, Red\\\", Green\", White, Purple, Yellow"),
                              Arrays.asList("Blue", "\"Black, Red\", Green", "White", "Purple", "Yellow"));
        
        assertListEquals("7", CSVFileHandler.splitCSVLine("Blue, \"Black\", Red, \"Green\\\"\", White, Purple, Yellow"),
                              Arrays.asList("Blue", "Black", "Red", "Green\"", "White", "Purple", "Yellow"));
        
        assertListEquals("8", CSVFileHandler.splitCSVLine("Blue, \"\"Black, Red\"\", Green, \"\"White\", Purple\", Yellow"),
                              Arrays.asList("Blue", "\"Black", "Red\"", "Green", "\"White\", Purple", "Yellow"));
        
        assertListEquals("9", CSVFileHandler.splitCSVLine("Blue, {Black, Red(Green, White)\"Purple, Yellow\"}"),
                              Arrays.asList("Blue", "{Black, Red(Green, White)\"Purple, Yellow\"}"));
        
        assertListEquals("10", CSVFileHandler.splitCSVLine("Blue, \"{Black, \\\"Red(Green}\", White, Purple, Yellow"),
                               Arrays.asList("Blue", "{Black, \"Red(Green}", "White", "Purple", "Yellow"));
        
        assertListEquals("11", CSVFileHandler.splitCSVLine("Blue, [Black, \"Red\", [Green(]], White, Purple, Yellow"),
                               Arrays.asList("Blue", "[Black, \"Red\", [Green(]]", "White", "Purple", "Yellow"));
        
        assertListEquals("12", CSVFileHandler.splitCSVLine("Blue, 'Black, Red', Green, White, Purple, Yellow"),
                               Arrays.asList("Blue", "'Black, Red'", "Green", "White", "Purple", "Yellow"));
        
        assertListEquals("13", CSVFileHandler.splitCSVLine("Blue, Black\\, Red, Green, White, Purple, Yellow"),
                               Arrays.asList("Blue", "Black, Red", "Green", "White", "Purple", "Yellow"));
        
        assertListEquals("14", CSVFileHandler.splitCSVLine("Blue, Black, \"Red, Green\\\", White\", Purple, Yellow"),
                               Arrays.asList("Blue", "Black", "Red, Green\", White", "Purple", "Yellow"));
        
        assertListEquals("15", CSVFileHandler.splitCSVLine("Blue, Black, \"Red, Green\"\", White\", Purple, Yellow"),
                               Arrays.asList("Blue", "Black", "Red, Green\", White", "Purple", "Yellow"));
    }
    
    @Test
    public void consolePrinter()
    {
        String line = "Blue, Black, Red, Green, White, Purple, Yellow";
        
        List<String> entries = CSVFileHandler.splitCSVLine(line);
        
        System.out.println("Members of line, as parsed: ");
        
        for(int i = 0; i < entries.size(); i++)
            System.out.println(" - " + entries.get(i));
    }
}