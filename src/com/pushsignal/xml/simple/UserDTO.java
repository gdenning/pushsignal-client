package com.pushsignal.xml.simple;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="user", strict=false)
public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Element
	private long userId;

	@Element
	private String name;

	// Only used by createUser and resetUser activities
	@Element(required=false)
	private String password;

	@Element(required=false)
	private String description;

	@Element
	private long points;

	public void setUserId(final long userId) {
		this.userId = userId;
	}

	public long getUserId() {
		return this.userId;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

	public void setPoints(final long points) {
		this.points = points;
	}

	public long getPoints() {
		return points;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (userId ^ (userId >>> 32));
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof UserDTO)) {
			return false;
		}
		final UserDTO other = (UserDTO) obj;
		if (userId != other.userId) {
			return false;
		}
		return true;
	}
}
