package com.enkigaming.lib.filehandling;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

// To do: Move the call to 

/**
 * Generic file handler.
 * Takes a passed file object and automates the process of actually writing to
 * and reading from the file.
 * @author Hanii Puppy <hanii.puppy@googlemail.com>
 * @note Pre-interpretation and post-interpretation happen regardless of whether or not there's actually a file to interpret.
 */
public abstract class FileHandler
{
    /**
     * The constructor. File corrupted message is generated using the file name.
     * @param file The file this handler should write to and read from.
     */
    public FileHandler(String handlerID, File file)
    { this(handlerID, file, null, "File Corrupted: " + file.getName()); }

    /**
     * The constructor.
     * @param file The file this handler should write to and read from.
     * @param CorruptFileMessage The message to be displayed upon establishing that a file's corrupt.
     */
    public FileHandler(String handlerID, File file, String corruptFileMessage)
    { this(handlerID, file, null, corruptFileMessage); }

    /**
     * The constructor. File corrupted message is generated using the file name.
     * @param file The file this handler should write to and read from.
     * @param logger The logger that messages should be sent to.
     */
    public FileHandler(String handlerID, File file, Logger logger)
    { this(handlerID, file, logger, "File Corrupted: " + file.getName()); }

    /**
     * The constructor.
     * @param file The file this handler should write to and read from.
     * @param logger The logger that messages should be sent to.
     * @param CorruptFileMessage The message to be displayed upon establishing that a file's corrupt.
     */
    public FileHandler(String handlerID, File file, Logger logger, String corruptFileMessage)
    {
        id = handlerID;
        handledFile = file;
        this.logger = logger;
        this.corruptFileMessage = corruptFileMessage;
    }


    final String id;
    final File handledFile;
    String corruptFileMessage;
    Logger logger;

    final Object fileBusy = new Object();

    final List<String> prerequisiteHandlers = new ArrayList<String>();
    
    public String getId()
    { return id; }
    
    /**
     * Gets the message to be displayed upon establishing that a file's corrupt.
     * @return The aforementioned message.
     */
    public String getCorruptFileMessage()
    { return corruptFileMessage; }

    /**
     * Gets the file this handler reads or writes to.
     * @return The aforementioned file.
     */
    public File getFile()
    {
        synchronized(fileBusy)
        { return handledFile; }
    }

    /**
     * Gets the logger used for sending messages.
     * @return The aforementioned logger.
     */
    public Logger getLogger()
    { return logger; }
    
    public Collection<String> getPrerequisiteHandlerIds()
    {
        synchronized(prerequisiteHandlers)
        { return new ArrayList<String>(prerequisiteHandlers); }
    }

    /**
     * Sets the message to be displayed upon establishing that a file's corrupt.
     * @param cfm The aforementioned message.
     */
    public void setCorruptFileMessage(String cfm)
    { corruptFileMessage = cfm; }

    /**
     * Sets the logger that should be used for sending messages.
     * @param logger The aforementioned logger.
     */
    public void setLogger(Logger logger)
    { this.logger = logger; }

    /**
     * Is called to allow any preparation of the file before being loaded to occur.
     */
    protected abstract void preSave();

    /**
     * Builds the save file.
     * This method isolates the logic for the construction of the file from the logic for writing to the file, which the handler handles.
     * @param writer The printwriter. This provides the print methods required to write to the file.
     */
    protected abstract void buildSaveFile(PrintWriter writer);

    /**
     * Is called to allow any cleaning-up of the file after being loaded to occur.
     */
    protected abstract void postSave();

    /**
     * Is called to allow any preparation of the file being loaded to occur.
     */
    protected abstract void preInterpretation();

    /**
     * Interprets the contents of the file.
     * This method isolates the logic for the interpretation of the file from the logic for reading from the file, which the handler handles.
     * @param Lines The contents of the file, with each line split up into a different string.
     * @return True if file loads flawlessly. False if the file is corrupted.
     */
    protected abstract boolean interpretFile(List<String> lines);

    /**
     * Is called to allow any cleaning-up of the file being loaded to occur.
     */
    protected abstract void postInterpretation();

    /**
     * What happens when the filehandler attempts to load a file that doesn't exist.
     * This should normally be used to flag this fact up on the console, and load default values if applicable.
     */
    protected abstract void onNoFileToInterpret();

    /**
     * Saves the file.
     */
    public void save()
    {
        try
        {
            synchronized(handledFile)
            {
                handledFile.mkdirs();
                    
                if(handledFile.exists())
                    handledFile.delete();

                handledFile.createNewFile();

                FileWriter fw = new FileWriter(handledFile, true);
                PrintWriter pw = new PrintWriter(fw);

                preSave();
                buildSaveFile(pw);
                postSave();

                pw.flush();
                pw.close();
                fw.close();
            }
        }
        catch(IOException exception)
        { exception.printStackTrace(); }
    }

    /**
     * Loads the file.
     */
    public void load()
    {
        try
        {
            synchronized(handledFile)
            {
                if(handledFile.exists())
                {
                    preInterpretation();

                    DataInputStream input = new DataInputStream(new FileInputStream(handledFile));
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    List<String> lines = new ArrayList<String>();

                    for(String i = ""; i != null; i = reader.readLine())
                        lines.add(i);

                    try
                    {
                        if(!interpretFile(lines))
                        {
                            copyFile(handledFile, new File(handledFile.getParentFile(), appendCorruptedNote(handledFile.getName())));
                            print(corruptFileMessage);
                        }
                    }
                    finally
                    {
                        postInterpretation();

                        input.close();
                        reader.close();
                    }
                }
                else
                {
                    try
                    {
                        preInterpretation();
                        onNoFileToInterpret();
                    }
                    finally
                    { postInterpretation(); }
                }
            }
        }
        catch(IOException exception)
        { exception.printStackTrace(); }
    }

    /**
     * Specifies that this filehandler should only ever load after another has already loaded.
     * @param HandlerID The ID of the filehandler to load after.
     */
    public void mustLoadAfterHandler(String handlerId)
    {
        if(handlerId == null)
            throw new IllegalArgumentException("handlerId cannot be null");
        
        synchronized(prerequisiteHandlers)
        { prerequisiteHandlers.add(handlerId); }
    }

    /**
     * Gets the file-name passed with the addition of a corrupted note specifying the date and time the file was deemed to be corrupt.
     * @param FileName The file-name to add the tag to.
     * @return The file-name with the tag appended.
     */
    String appendCorruptedNote(String fileName)
    {
        Date now = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH|mm|ss");
        String CorruptedNote = " (Corrupted " + format.format(now) + ")";

        String[] FileNameParts = handledFile.getName().split("\\.");

        if(FileNameParts.length > 1)
        {
            String FileNameWithoutExtension = "";
            String Extension = FileNameParts[FileNameParts.length - 1];

            for(int i = 0; i < FileNameParts.length - 1; i++)
                FileNameWithoutExtension = FileNameWithoutExtension + FileNameParts[i];

            return FileNameWithoutExtension + CorruptedNote + "." + Extension;
        }
        else
            return FileNameParts[0] + CorruptedNote;
    }

    /**
     * Makes a copy of a few in a new path.
     * @param sourceFile The file to copy.
     * @param destFile The file to copy the source file to.
     * @throws IOException Input and output related errors pertaining to the file input and file output streams as a result of the functionality of this method.
     */
    void copyFile(File sourceFile, File destFile) throws IOException
    {
        if(!destFile.exists())
            destFile.createNewFile();

        FileChannel source = null;
        FileChannel destination = null;

        try
        {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        }
        finally
        {
            if(source != null)
                source.close();

            if(destination != null)
                destination.close();
        }
    }
    
    public void print(String toPrint)
    {
        if(logger != null)
            logger.info(toPrint);
        else
            System.out.println(toPrint);
    }
}