package com.enkigaming.lib.filehandling;

// VLS = Variable Length Sets. Pretty much sums up the filetype.

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang3.NotImplementedException;


/**
 * Generic variable-length-set file handler. (specific to bukkit)
 * Takes a file and automates the process of writing to the file as well as the
 * logic required to format that file as a VLS file.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 * @note Pre-interpretation and post-interpretation happen regardless of whether or not there's actually a file to interpret.
 */
public abstract class VLSFileHandler extends FileHandler
{
    public static class VLSDataSet
    {
        public VLSDataSet(String name, List<String> values)
        {
            this.name = name;
            this.values = new ArrayList<String>(values);
        }
        
        public String name;
        public List<String> values;
        
        public String getName()
        { return name; }
        
        public List<String> getValues()
        { return values; }
    }
    
    /**
     * Constructs the file-handler.
     * @param HandlerID The unique ID of the file-handler.
     * @param file The file to have data written to it.
     */
    public VLSFileHandler(String HandlerID, File file)
    { super(HandlerID, file); }

    /**
     * Constructs the file-handler.
     * @param HandlerID The unique ID of the file-handler.
     * @param file The file to have data written to it.
     * @param CorruptFileMessage The message to display upon establishing that a file to be loaded is corrupt.
     */
    public VLSFileHandler(String HandlerID, File file, String CorruptFileMessage)
    { super(HandlerID, file, CorruptFileMessage); }

    /**
     * Constructs the file-handler.
     * @param HandlerID The unique ID of the file-handler.
     * @param file The file to have data written to it.
     * @param logger The logger to send console messages to.
     */
    public VLSFileHandler(String HandlerID, File file, Logger logger)
    { super(HandlerID, file, logger); }

    /**
     * Constructs the file-handler.
     * @param HandlerID The unique ID of the file-handler.
     * @param file The file to have data written to it.
     * @param logger The logger to send console messages to.
     * @param CorruptFileMessage The message to display upon establishing that a file to be loaded is corrupt.
     */
    public VLSFileHandler(String HandlerID, File file, Logger logger, String CorruptFileMessage)
    { super(HandlerID, file, logger, CorruptFileMessage); }

    @Override
    protected boolean interpretFile(List<String> Lines)
    {
        List<String> lines = new ArrayList<String>();
        lines.addAll(Lines);
        List<String> LinesToRemove = new ArrayList<String>();
        boolean Corrupt = false;

        String[] LineByColon;
        String[] ValuesByComma;
        String[] ValuesByCommaTrimmed;

        for(String i : lines)
        {
            if(i.startsWith("#") || i.trim().equalsIgnoreCase(""))
                LinesToRemove.add(i);
        }

        for(String i : LinesToRemove)
            lines.remove(i);

        for(String i : lines)
        {
            LineByColon = i.split(":", 2);
            //ValuesByComma = LineByColon[1].trim().split(",");
            ValuesByComma = SplitByNotInBrackets(LineByColon[1].trim(), ",");
            ValuesByCommaTrimmed = new String[ValuesByComma.length];

            for(int j = 0; j < ValuesByComma.length; j++)
                ValuesByCommaTrimmed[j] = ValuesByComma[j].trim();

            if(!InterpretValues(LineByColon[0], ValuesByCommaTrimmed))
                Corrupt = true;
        }

        return !Corrupt;
    }

    @Override
    protected abstract void preInterpretation();

    /**
     * Interprets the passed line, decodes/parses it from the VLS format, and loads the data appropriately.
     * @param DataSetName The name/title/identifier of the current data set.
     * @param Values The values in the data set.
     * @return True if the line was interpreted correctly. False if the line was corrupt or unreadable.
     */
    protected abstract boolean InterpretValues(String DataSetName, String... Values);

    @Override
    protected abstract void postInterpretation();

    @Override
    protected abstract void onNoFileToInterpret();
    
    @Override
    protected void buildSaveFile(PrintWriter writer)
    {
        boolean FinishUp = false;
        int Counter = 0;
        VLSDataSet temp;
        String ToPrint;
        String Splitter;

        while(!FinishUp)
        {
            temp = getValues(Counter);

            if(temp != null)
            {
                Counter++;
                ToPrint = temp.getName() + ": ";
                Splitter = "";

                for(String i : temp.getValues())
                {
                    ToPrint += Splitter + i;
                    Splitter = ", ";
                }

                writer.println(ToPrint);
            }
            else
                FinishUp = true;
        }
    }
    
    
    /**
     * Splits a passed string into substrings by instances of the other passed string when not in any brackets. (That is, (), {}, [], or <>)
     * @param ToSplit The string to split into substrings.
     * @param ToSplitBy The string by which to split the ToSplit string.
     * @return An array containing the substrings, the result of the split.
     */
    public static String[] SplitByNotInBrackets(String ToSplit, String ToSplitBy)
    {
        if(ToSplitBy.length() > 1) throw new NotImplementedException("SplitByNotInBrackets(String, String); currently doesn't support splitting by multi-character strings.");

        if(ToSplitBy.equals("(")
        || ToSplitBy.equals("{")
        || ToSplitBy.equals("[")
        || ToSplitBy.equals("<")
        || ToSplitBy.equals(")")
        || ToSplitBy.equals("}")
        || ToSplitBy.equals("]")
        || ToSplitBy.equals(">"))
        { throw new IllegalArgumentException("ToSplitBy must not be a bracket."); }

        if(ToSplitBy.equals(""))
            throw new IllegalArgumentException("ToSplitBy must not be blank.");

        char SplitChar = ToSplitBy.charAt(0);
        int BracketCount = 0;
        int CurlyBracketCount = 0;
        int SquareBracketCount = 0;
        int PointyBracketCount = 0;
        List<Integer> SplitPoints = new ArrayList<Integer>();

        for(int i = 0; i < ToSplit.length(); i++)
        {
            char ichar = ToSplit.charAt(i);

            if(ichar == '(')
                BracketCount++;
            else if(ichar == '{')
                CurlyBracketCount++;
            else if(ichar == '[')
                SquareBracketCount++;
            else if(ichar == '<')
                PointyBracketCount++;
            else if(ichar == ')' && BracketCount > 0)
                BracketCount--;
            else if(ichar == '}' && BracketCount > 0)
                CurlyBracketCount--;
            else if(ichar == ']' && BracketCount > 0)
                SquareBracketCount--;
            else if(ichar == '>' && BracketCount > 0)
                PointyBracketCount--;
            else if(ichar == SplitChar && BracketCount <= 0 && CurlyBracketCount <= 0 && SquareBracketCount <= 0 && PointyBracketCount <= 0)
                SplitPoints.add(i);
        }

        int LastSplit = -1;
        List<String> strings = new ArrayList<String>();

        for(int i = 0; i < SplitPoints.size(); i++)
        {
            strings.add(ToSplit.substring(LastSplit + 1, SplitPoints.get(i)));
            LastSplit = SplitPoints.get(i);
        }

        strings.add(ToSplit.substring(LastSplit + 1));

        return strings.toArray(new String[0]);
    }

    /**
     * Gets the value to be at the line marked by the value counter.
     * @param ValueCounter The iteration of data to get.
     * @return A pair containing the set name/title/identifier as a string, and a list of strings, being the values represented under that name/title/identifier.
     */
    protected abstract VLSDataSet getValues(int ValueCounter);
}