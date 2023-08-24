package org.amshove.natparse.natural;

import javax.annotation.Nullable;

public interface IFunction extends INaturalModule, IModuleWithBody, IHasDefineData
{
	@Nullable
	IDataType returnType();
}
