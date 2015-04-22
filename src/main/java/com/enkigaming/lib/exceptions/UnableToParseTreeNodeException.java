package com.enkigaming.lib.exceptions;

import com.enkigaming.lib.filehandling.TreeFileHandler.TreeNode;

public class UnableToParseTreeNodeException extends UnableToParseException
{
    public UnableToParseTreeNodeException(TreeNode treeNode)
    { super(treeNode); }
    
    public UnableToParseTreeNodeException(TreeNode treeNode, String message)
    { super(treeNode, message); }
    
    @Override
    public TreeNode getObjectUnableToParse()
    { return (TreeNode)super.getObjectUnableToParse(); }
}