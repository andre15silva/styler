/*
 * Copyright (C) 2018-2019 Google, LLC.
 *
 * License: https://opensource.org/licenses/BSD-3-Clause
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jflex.testcase.apipirivate;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import jflex.testing.testsuite.JFlexTestRunner;
import jflex.testing.testsuite.annotations.TestSpec;
import jflex.util.javac.CompilerException;
import jflex.util.javac.JavacUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test that JFlex can generate private classes.
 *
 * <p>See feature request #513500 (Generate cleaner interfaces), {@code %apiprivate} option
 */
@RunWith(JFlexTestRunner.class)
@TestSpec(lex = "javatests/jflex/testcase/apipirivate/private.flex")
public class ApiPrivateTest {

  @Test
  public void compile() throws CompilerException {
    try {
      JavacUtils.compile(
          ImmutableList.of(
              "javatests/jflex/testcase/apipirivate/Private.java",
              "javatests/jflex/testcase/apipirivate/AttemptPrivateAccess.java"));
      fail("Class `Private` should have private access");
    } catch (CompilerException e) {
      assertThat(e).hasMessageThat().contains("compiler.err.report.access");
    }
  }
}
