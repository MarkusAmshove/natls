package org.amshove.natlint.natparse.parsing.ddm;

import org.amshove.natlint.natparse.NaturalParseException;
import org.amshove.natlint.natparse.natural.DataFormat;
import org.amshove.natlint.natparse.natural.ddm.DescriptorType;
import org.amshove.natlint.natparse.natural.ddm.FieldType;
import org.amshove.natlint.natparse.natural.ddm.NullValueSuppression;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

public class GroupFieldShould
{
	@Test
	void protectAgainstNonGroupFields()
	{
		assertThatExceptionOfType(NaturalParseException.class)
			.isThrownBy(() -> new GroupField(new DdmField(FieldType.NONE, 1, "", "", DataFormat.ALPHANUMERIC, 0, NullValueSuppression.NONE, DescriptorType.DESCRIPTOR, "")))
			.withMessage("Cannot promote field of type NONE to GroupField");
	}
}
