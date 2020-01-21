package org.yeauty.support;

import io.netty.channel.Channel;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;


public interface MethodArgumentResolver {

	/**
	 * Whether the given {@linkplain MethodParameter method parameter} is
	 * supported by this resolver.
	 * @param parameter the method parameter to check
	 * @return {@code true} if this resolver supports the supplied parameter;
	 * {@code false} otherwise
	 */
	boolean supportsParameter(MethodParameter parameter);


	@Nullable
	Object resolveArgument(MethodParameter parameter, Channel channel,Object object) throws Exception;

}
