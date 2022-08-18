package org.amshove.natqube.ruletranslator;

public class App
{
	public static void main(String[] args)
	{
		RuleRepository.getRules().forEach(System.out::println);
	}
}
