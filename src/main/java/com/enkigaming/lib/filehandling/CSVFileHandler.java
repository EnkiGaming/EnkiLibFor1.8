package com.enkigaming.lib.filehandling;

// To do: Swap out the CSV-line handling mechanism with a more thorough one.

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// To do: Check Javadoc.

/**
 * Generic comma-separated-value file handler.
 * Takes a file and automates the process of writing to the file as well as the
 * logic required to format that file as a CSV file.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 */
public abstract class CSVFileHandler extends FileHandler
{
    public static class CSVRowMember
    {
        public CSVRowMember(String contents, boolean wrapInQuotes)
        {
            this.contents = contents;
            this.wrapInQuotes = wrapInQuotes;
        }
        
        protected String contents;
        protected final boolean wrapInQuotes;
        
        public String getContents()
        { return contents; }
        
        void setContents(String newContents)
        { contents = newContents; }
        
        public boolean shouldBeWrappedInQuotes()
        { return wrapInQuotes; }
    }
    
    /**
     * The constructor. File corrupted message is generated using the file name.
     * @param file The file this handler should write to and read from.
     */
    public CSVFileHandler(String HandlerID, File file)
    { super(HandlerID, file); }

    /**
     * The constructor.
     * @param file The file this handler should write to and read from.
     * @param CorruptFileMessage The message to be displayed upon establishing that a file's corrupt.
     */
    public CSVFileHandler(String HandlerID, File file, String CorruptFileMessage)
    { super(HandlerID, file, CorruptFileMessage); }

    /**
     * The constructor. File corrupted message is generated using the file name.
     * @param file The file this handler should write to and read from.
     * @param logger The logger that messages should be sent to.
     */
    public CSVFileHandler(String HandlerID, File file, Logger logger)
    { super(HandlerID, file, logger); }

    /**
     * The constructor.
     * @param file The file this handler should write to and read from.
     * @param logger The logger that messages should be sent to.
     * @param CorruptFileMessage The message to be displayed upon establishing that a file's corrupt.
     */
    public CSVFileHandler(String HandlerID, File file, Logger logger, String CorruptFileMessage)
    { super(HandlerID, file, logger, CorruptFileMessage); }

    
    @Override
    protected boolean interpretFile(List<String> Lines)
    {
        boolean Corrupt = false;
        List<String> Entries = new ArrayList<String>();

        stripEmptyLines(Lines);

        if(Lines.isEmpty())
            Corrupt = true;
        
        if(!Lines.get(0).equalsIgnoreCase(getHeader()))
            Corrupt = true;

        for(int i = 1; i < Lines.size(); i++)
        {
            List<String> Fields = splitCSVLine(Lines.get(i));
            boolean LineIsCorrupt = false;

            if(!interpretRow(Fields))
                Corrupt = true;
        }

        return !Corrupt;
    }

    @Override
    protected abstract void onNoFileToInterpret();

    String getHeader()
    {
        List<String> ColumnNames = getColumnNames();

        if(ColumnNames.size() <= 0)
            throw new RuntimeException("There should be at least one column in a CSV file.");

        String Header = ColumnNames.get(0);

        for(int i = 1; i < ColumnNames.size(); i++)
            Header += ("," + ColumnNames.get(i));

        return Header;
    }

    void stripEmptyLines(List<String> lines)
    {
        List<String> LinesToRemove = new ArrayList<String>();

        for(String i : lines)
            if(i.trim().equals(""))
                LinesToRemove.add(i);

        for(String i : LinesToRemove)
            lines.remove(i);
    }
    
    private static enum PositionState // Because Java doesn't support local enums.
    { inQuotes, inInvertedCommas, inBrackets, inSquareBrackets, inCurlyBrackets, inChevronBrackets }

    static List<String> splitCSVLine(String toSplit)
    {
        List<String> entries = new ArrayList<String>();
        boolean nextCharIsEscaped = false;
        boolean lastCharIsEscapingQuote = false;
        StringBuilder entryBuilder = new StringBuilder();
        List<PositionState> positionStates = new ArrayList<PositionState>();
        
        for(int i = 0; i < toSplit.length(); i++)
        {
            char iChar = toSplit.charAt(i);
            boolean printCharacter = true; // Whether or not the character should be printed into the current entry.
            boolean thisCharShouldBeEscaped = nextCharIsEscaped;
            nextCharIsEscaped = false;
            
            switch(iChar)
            {
                case '\\':
                {
                    if(!thisCharShouldBeEscaped)
                    {
                        printCharacter = false;
                        nextCharIsEscaped = true;
                    }
                } break;
                    
                case ',':
                {
                    if(!thisCharShouldBeEscaped && positionStates.isEmpty())
                    {
                        entries.add(entryBuilder.toString());
                        entryBuilder = new StringBuilder();
                        printCharacter = false;
                    }
                } break;
                    
                case '\'':
                {
                    if(!thisCharShouldBeEscaped)
                    {
                        boolean isOpening = true;

                        for(int j = positionStates.size() - 1; j >= 0; j--)
                            if(positionStates.get(j) == PositionState.inInvertedCommas)
                            {
                                isOpening = false;
                                
                                for(int k = positionStates.size() - 1; k >= j; k--)
                                    positionStates.remove(k);
                                
                                break;
                            }
                        
                        if(isOpening)
                            positionStates.add(PositionState.inInvertedCommas);
                    }
                } break;
                    
                case '"':
                {
                    if(thisCharShouldBeEscaped)
                        break;
                    
                    if(lastCharIsEscapingQuote)
                        lastCharIsEscapingQuote = false;
                    else if(i < toSplit.length() - 1 && toSplit.charAt(i + 1) == '"')
                        lastCharIsEscapingQuote = true;
                    else
                    {
                        boolean isOpening = true;

                        for(int j = positionStates.size() - 1; j >= 0; j--)
                            if(positionStates.get(j) == PositionState.inQuotes)
                            {
                                isOpening = false;

                                for(int k = positionStates.size() - 1; k >= j; k--)
                                    positionStates.remove(k);

                                break;
                            }

                        if(isOpening)
                            positionStates.add(PositionState.inQuotes);
                    }
                } break;
                    
                case '(':
                {
                    if(!thisCharShouldBeEscaped)
                        positionStates.add(PositionState.inBrackets);
                } break;
                    
                case ')':
                {
                    if(!thisCharShouldBeEscaped)
                        for(int j = positionStates.size() - 1; j >= 0; j--)
                            if(positionStates.get(j) == PositionState.inBrackets)
                            {
                                for(int k = positionStates.size() - 1; k >= j; k--)
                                    positionStates.remove(k);

                                break;
                            }
                } break;
                    
                case '[':
                {
                    if(!thisCharShouldBeEscaped)
                        positionStates.add(PositionState.inSquareBrackets);
                } break;
                    
                case ']':
                {
                    if(!thisCharShouldBeEscaped)
                        for(int j = positionStates.size() - 1; j >= 0; j--)
                            if(positionStates.get(j) == PositionState.inSquareBrackets)
                            {
                                for(int k = positionStates.size() - 1; k >= j; k--)
                                    positionStates.remove(k);

                                break;
                            }
                } break;
                    
                case '{':
                {
                    if(!thisCharShouldBeEscaped)
                        positionStates.add(PositionState.inCurlyBrackets);
                } break;
                    
                case '}':
                {
                    if(!thisCharShouldBeEscaped)
                        for(int j = positionStates.size() - 1; j >= 0; j--)
                            if(positionStates.get(j) == PositionState.inCurlyBrackets)
                            {
                                for(int k = positionStates.size() - 1; k >= j; k--)
                                    positionStates.remove(k);

                                break;
                            }
                } break;
            }
            
            if(printCharacter)
                entryBuilder.append(iChar);
        }
        
        entries.add(entryBuilder.toString());
        handleQuotes(entries);
        return entries;
    }
    
    /**
     * Removes the surrounding quotes from entries that are enclosed in them, and converts adjacent pairs of quotation
     * marks into single ones, where they don't represent an empty field and the first quotation mark isn't escaped.
     * @param entries The list of entries to handle quotes in.
     */
    private static void handleQuotes(List<String> entries)
    {
        for(int i = 0; i < entries.size(); i++)
        {
            String iEntry = entries.get(i);
            String iEntryOriginal = iEntry;
            
            iEntry = iEntry.trim();
            
            if(iEntry.startsWith("\"") && iEntry.endsWith("\""))
                iEntry = iEntry.substring(1, iEntry.length() - 1);
            
            /*
                Go through iEntry looking for double quotes (""). Double-quotes representing empty fields have already
                been taken out with the previous check. Replace them with a single quote (") where the first quote isn't
                escaped. That is, where they aren't preceded by an escape character (\) that isn't itself escaped.
                This can be approximated by checking whether they're preceded by an odd or even number of the escape
                character.
            */
            
            for(int j = 0; j < iEntry.length() - 1; j++)
            {
                if(iEntry.charAt(j) == '"' && iEntry.charAt(j + 1) == '"')
                {
                    int escapeCharacterCount = 0;
                    
                    for(int k = j - 1; k >= 0; k--)
                    {
                        if(iEntry.charAt(k) == '\\')
                            escapeCharacterCount++;
                        else
                            break;
                    }
                    
                    if(escapeCharacterCount % 2 == 0) // if escapeCharacterCount is even or 0
                        iEntry = new StringBuilder(iEntry).deleteCharAt(j + 1).toString();
                }
            }
            
            if(!iEntry.equals(iEntryOriginal))
                entries.set(i, iEntry);
        }
    }

    /**
     * Gets a list of strings containing the names of each of the columns.
     * @return The aforementioned list.
     */
    protected abstract List<String> getColumnNames();

    /**
     * Action to take before the rows in the file are interpreted.
     */
    @Override
    protected abstract void preInterpretation();

    /**
     * Takes a list of strings containing the fields of a row from the file (in order from left to right) and handles the
     * strings as intended.
     * @param Row The list of strings representing the fields from a row in the file.
     * @return True if the row was interpreted correctly; false if the row was corrupt.
     */
    protected abstract boolean interpretRow(List<String> Row);

    /**
     * Action to take immediately after the rows in the file have been interpreted.
     */
    @Override
    protected abstract void postInterpretation();
    
    @Override
    protected void buildSaveFile(PrintWriter writer)
    {
        writer.println(getHeader());

        List<CSVRowMember> Fields;
        boolean Finished = false;
        String Row;

        for(int i = 0; !Finished; i++)
        {
            Fields = getRow(i);

            if(Fields == null)
                Finished = true;
            else
            {
                if(Fields.size() > 0)
                {
                    for(CSVRowMember j : Fields)
                        if(j.shouldBeWrappedInQuotes())
                            j.setContents("\"" + j.getContents() + "\"");

                    Row = Fields.get(0).getContents();

                    for(int j = 1; j < Fields.size(); j++)
                        Row += ("," + Fields.get(j).getContents());

                    writer.println(Row);
                }
                else
                    writer.println("");
            }
        }
    }

    @Override
    protected abstract void preSave();

    /**
     * Gets a list containing pairs, each representing an entry in the row.
     * The string represents the text, and the boolean represents whether or not the string should be in quotes.
     * @param RowNumber The number of the row in the data set requested.
     * @return The row requested, or null if the the RowNumber is past the end of the dataset.
     */
    protected abstract List<CSVRowMember> getRow(int RowNumber);

    @Override
    protected abstract void postSave();
}