package org.amshove.natparse.parsing.ddm;

import org.amshove.natparse.NaturalParseException;
import org.amshove.natparse.natural.DataFormat;
import org.amshove.natparse.natural.ddm.DescriptorType;
import org.amshove.natparse.natural.ddm.FieldType;
import org.amshove.natparse.natural.ddm.NullValueSuppression;
import org.amshove.natparse.parsing.ddm.DdmField;
import org.amshove.natparse.parsing.ddm.GroupField;
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
