package com.enkigaming.lib.filehandling;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public abstract class TreeFileHandler extends FileHandler
{
    public static class TreeNode
    {
        public TreeNode(String name)
        { this.name = name; }
        
        protected String name;
        protected List<TreeNode> children = new ArrayList<TreeNode>();
        
        public String getName()
        { return name; }
        
        public String setName(String newName)
        {
            String old = name;
            name = newName;
            return old;
        }
        
        public boolean addChild(TreeNode member)
        { return children.add(member); }
        
        public boolean addChildren(Collection<TreeNode> membersToAdd)
        { return children.addAll(membersToAdd); }
        
        public List<TreeNode> getChildren()
        { return new ArrayList<TreeNode>(children); }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            final TreeNode other = (TreeNode) obj;
            if((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
                return false;
            if(this.children != other.children && (this.children == null || !this.children.equals(other.children)))
                return false;
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 37 * hash + (this.children != null ? this.children.hashCode() : 0);
            return hash;
        }
    }
    
    protected static class NameIndentLevelPair
    {
        public NameIndentLevelPair(String name, int indentLevel)
        {
            this.name = name;
            this.indentLevel = indentLevel;
        }
        
        protected String name;
        protected int indentLevel;
        
        public String getName()
        { return name; }
        
        public void setName(String newName)
        { name = newName; }
        
        public int getIndentLevel()
        { return indentLevel; }
        
        public void setIndentLevel(int newIndentLevel)
        { indentLevel = newIndentLevel; }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            final NameIndentLevelPair other = (NameIndentLevelPair) obj;
            if((this.name == null) ? (other.name != null) : !this.name.equals(other.name))
                return false;
            if(this.indentLevel != other.indentLevel)
                return false;
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
            hash = 17 * hash + this.indentLevel;
            return hash;
        }
    }
    
    protected static class TreeMemberPairListPair
    {
        public TreeMemberPairListPair(TreeNode member, List<NameIndentLevelPair> pairs)
        {
            this.member = member;
            this.pairs = new ArrayList<NameIndentLevelPair>(pairs);
        }
        
        protected final TreeNode member;
        protected final List<NameIndentLevelPair> pairs;
        
        public TreeNode getMember()
        { return member; }
        
        public List<NameIndentLevelPair> getPairs()
        { return pairs; }

        @Override
        public boolean equals(Object obj)
        {
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            final TreeMemberPairListPair other = (TreeMemberPairListPair) obj;
            if(this.member != other.member && (this.member == null || !this.member.equals(other.member)))
                return false;
            if(this.pairs != other.pairs && (this.pairs == null || !this.pairs.equals(other.pairs)))
                return false;
            return true;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 73 * hash + (this.member != null ? this.member.hashCode() : 0);
            hash = 73 * hash + (this.pairs != null ? this.pairs.hashCode() : 0);
            return hash;
        }
    }

    public TreeFileHandler(String handlerId, File file)
    { super(handlerId, file); }
    
    public TreeFileHandler(String handlerId, File file, String corruptFileMessage)
    { super(handlerId, file, corruptFileMessage); }
    
    public TreeFileHandler(String handlerId, File file, Logger logger)
    { super(handlerId, file, logger); }
    
    public TreeFileHandler(String handlerId, File file, Logger logger, String corruptFileMessage)
    { super(handlerId, file, logger, corruptFileMessage); }
    
    protected String indentLevelText = "    ";
    
    @Override
    protected abstract void preSave();

    @Override
    protected void buildSaveFile(PrintWriter writer)
    {
        List<TreeNode> baseMembers = getTreeStructureOfSaveData();
        
        for(TreeNode baseMember : baseMembers)
            printMemberAndSubmembers(writer, 0, baseMember);
    }
    
    protected abstract List<TreeNode> getTreeStructureOfSaveData();
    
    protected void printMemberAndSubmembers(PrintWriter writer, int indentLevel, TreeNode member)
    {
        writer.println(appendIndent(member.getName(), indentLevel));
        
        for(TreeNode submember : member.getChildren())
            printMemberAndSubmembers(writer, indentLevel + 1, submember);
    }

    @Override
    protected abstract void postSave();

    @Override
    protected abstract void preInterpretation();

    @Override
    protected boolean interpretFile(List<String> lines)
    {
        lines = stripEmptyLines(lines);
        List<NameIndentLevelPair> values = new ArrayList<NameIndentLevelPair>();
        
        for(String line : lines)
            values.add(getValue(new NameIndentLevelPair(line, 0)));
        
        fixLevels(values);
        
        return interpretTree(getTree(values));
    }
    
    protected void fixLevels(List<NameIndentLevelPair> values)
    {
        int currentMaxIndent = 0;
        
        for(int i = 0; i < values.size(); i++)
        {
            NameIndentLevelPair current = values.get(i);
            boolean done = false;
            
            for(int j = 0; j <= currentMaxIndent && !done; j++)
            {
                if(current.getName().startsWith(indentLevelText))
                {
                    current.setName(current.getName().substring(indentLevelText.length()));
                    current.setIndentLevel(current.getIndentLevel() + 1);
                }
                else
                    done = true;
            }
            
            currentMaxIndent = current.getIndentLevel() + 1;
        }
    }
    
    List<String> stripEmptyLines(List<String> lines)
    {
        List<String> linesToKeep = new ArrayList<String>();

        for(int i = 0; i < lines.size(); i++)
            if(!lines.get(i).trim().isEmpty())
                linesToKeep.add(lines.get(i));
        return linesToKeep;
    }
    
    protected abstract boolean interpretTree(List<TreeNode> tree);
    
    protected List<TreeNode> getTree(List<NameIndentLevelPair> values)
    {
        TreeNode baseNode = new TreeNode("This should never appear.");
        
        List<TreeNode> currentHierarchy = new ArrayList<TreeNode>();
        currentHierarchy.add(baseNode);
        
        for(int i = 0; i < values.size(); i++)
        {
            boolean assigned = false;
            
            while(!assigned)
            {
                if(values.get(i).getIndentLevel() >= currentHierarchy.size() - 1)
                {
                    TreeNode node = new TreeNode(values.get(i).getName());
                    currentHierarchy.get(currentHierarchy.size() - 1).addChild(node);
                    currentHierarchy.add(node);
                    assigned = true;
                }
                else
                    currentHierarchy.remove(currentHierarchy.size() - 1);
            }
        }
        
        return baseNode.getChildren();
    }
    
    protected String appendIndent(String string, int indentLevelToAppend)
    {
        String appendedString = string;
        
        for(int i = 0; i < indentLevelToAppend; i++)
            appendedString = indentLevelText + appendedString;
        
        return appendedString;
    }
    
    protected NameIndentLevelPair getValue(NameIndentLevelPair line)
    {
        if(line.getName().startsWith(indentLevelText))
            return new NameIndentLevelPair(line.getName().substring(indentLevelText.length()), line.getIndentLevel() + 1);
        else
            return line;
    }

    @Override
    protected abstract void postInterpretation();

    @Override
    protected abstract void onNoFileToInterpret();
    
    public String setIndentLevelText(String newIndentLevelText)
    {
        String old = indentLevelText;
        indentLevelText = newIndentLevelText;
        return old;
    }
}