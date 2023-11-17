package org.amshove.natls.languageserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UnresolvedCompletionInfo
{
	private String qualifiedName;
	private String uri;
	private List<String> previousTexts = new ArrayList<>();

	public UnresolvedCompletionInfo(String qualifiedName, String uri)
	{
		this.qualifiedName = qualifiedName;
		this.uri = uri;
	}

	public String getQualifiedName()
	{
		return qualifiedName;
	}

	public String getUri()
	{
		return uri;
	}

	public void setQualifiedName(String qualifiedName)
	{
		this.qualifiedName = qualifiedName;
	}

	public void setUri(String uri)
	{
		this.uri = uri;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		var that = (UnresolvedCompletionInfo) obj;
		return Objects.equals(this.qualifiedName, that.qualifiedName) &&
			Objects.equals(this.uri, that.uri);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(qualifiedName, uri);
	}

	@Override
	public String toString()
	{
		return "UnresolvedCompletionInfo[" +
			"qualifiedName=" + qualifiedName + ", " +
			"uri=" + uri + ']';
	}

	public List<String> getPreviousTexts()
	{
		return previousTexts;
	}

	public void setPreviousTexts(List<String> previousTexts)
	{
		this.previousTexts = previousTexts;
	}

	public void addPreviousText(String text)
	{
		if (text != null)
		{
			previousTexts.add(text);
		}
	}

	public boolean hasPreviousText(String text)
	{
		return previousTexts.contains(text.toUpperCase());
	}
}
