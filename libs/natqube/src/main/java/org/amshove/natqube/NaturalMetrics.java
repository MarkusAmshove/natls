package org.amshove.natqube;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metrics;

import java.util.Arrays;
import java.util.List;

import static org.sonar.api.measures.CoreMetrics.DOMAIN_SIZE;

public class NaturalMetrics implements Metrics
{
	private static final String NUMBER_OF_SUBPROGRAMS_KEY = "natural_size_number_sub_programs";
	public static final Metric<Integer> NUMBER_OF_SUBPROGRAMS = new Metric.Builder(
		NUMBER_OF_SUBPROGRAMS_KEY, "Subprograms", Metric.ValueType.INT
	)
		.setDescription("Number of Subprograms")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_PROGRAMS_KEY = "natural_size_number_programs";
	public static final Metric<Integer> NUMBER_OF_PROGRAMS = new Metric.Builder(
		NUMBER_OF_PROGRAMS_KEY, "Programs", Metric.ValueType.INT
	)
		.setDescription("Number of programs")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_EXTERNAL_SUBROUTINES_KEY = "natural_size_number_external_subroutines";
	public static final Metric<Integer> NUMBER_OF_EXTERNAL_SUBROUTINES = new Metric.Builder(
		NUMBER_OF_EXTERNAL_SUBROUTINES_KEY, "External Subroutines", Metric.ValueType.INT
	)
		.setDescription("Number of external subroutines")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_FUNCTIONS_KEY = "natural_size_number_functions";
	public static final Metric<Integer> NUMBER_OF_FUNCTIONS = new Metric.Builder(
		NUMBER_OF_FUNCTIONS_KEY, "Functions", Metric.ValueType.INT
	)
		.setDescription("Number of functions")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_DDMS_KEY = "natural_size_number_ddms";
	public static final Metric<Integer> NUMBER_OF_DDMS = new Metric.Builder(
		NUMBER_OF_DDMS_KEY, "DDMs", Metric.ValueType.INT
	)
		.setDescription("Number of DDMs")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_LDAS_KEY = "natural_size_number_ldas";
	public static final Metric<Integer> NUMBER_OF_LDAS = new Metric.Builder(
		NUMBER_OF_LDAS_KEY, "LDAs", Metric.ValueType.INT
	)
		.setDescription("Number of LDAs")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_GDAS_KEY = "natural_size_number_gdas";
	public static final Metric<Integer> NUMBER_OF_GDAS = new Metric.Builder(
		NUMBER_OF_GDAS_KEY, "GDAs", Metric.ValueType.INT
	)
		.setDescription("Number of GDAs")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_PDAS_KEY = "natural_size_number_pdas";
	public static final Metric<Integer> NUMBER_OF_PDAS = new Metric.Builder(
		NUMBER_OF_PDAS_KEY, "PDAs", Metric.ValueType.INT
	)
		.setDescription("Number of PDAs")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_COPYCODES_KEY = "natural_size_number_copy_codes";
	public static final Metric<Integer> NUMBER_OF_COPYCODES = new Metric.Builder(
		NUMBER_OF_COPYCODES_KEY, "Copy Codes", Metric.ValueType.INT
	)
		.setDescription("Number of Copy Codes")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_MAPS_KEY = "natural_size_number_maps";
	public static final Metric<Integer> NUMBER_OF_MAPS = new Metric.Builder(
		NUMBER_OF_MAPS_KEY, "Maps", Metric.ValueType.INT
	)
		.setDescription("Number of Maps")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	private static final String NUMBER_OF_HELPROUTINES_KEY = "natural_size_number_helproutines";
	public static final Metric<Integer> NUMBER_OF_HELPROUTINES = new Metric.Builder(
		NUMBER_OF_HELPROUTINES_KEY, "Helproutines", Metric.ValueType.INT
	)
		.setDescription("Number of Helproutines")
		.setDomain(DOMAIN_SIZE)
		.setQualitative(true)
		.create();

	@Override
	public List<Metric> getMetrics()
	{
		return Arrays.asList(
			NUMBER_OF_DDMS,
			NUMBER_OF_SUBPROGRAMS,
			NUMBER_OF_PROGRAMS,
			NUMBER_OF_EXTERNAL_SUBROUTINES,
			NUMBER_OF_HELPROUTINES,
			NUMBER_OF_GDAS,
			NUMBER_OF_LDAS,
			NUMBER_OF_PDAS,
			NUMBER_OF_MAPS,
			NUMBER_OF_COPYCODES,
			NUMBER_OF_FUNCTIONS
		);
	}
}
