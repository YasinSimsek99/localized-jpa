/**
 * Maven plugin for auto-configuring the compiler with LocalizedJPA requirements.
 * 
 * <p>This plugin automatically detects when a project uses LocalizedJPA and configures
 * the maven-compiler-plugin with:
 * <ul>
 *   <li>Annotation processor path for {@code localized-jpa-compiler}</li>
 *   <li>Required {@code --add-exports} JVM arguments for Javac internal API access</li>
 *   <li>Fork mode enabled for compiler execution</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * <plugin>
 *     <groupId>com.localizedjpa</groupId>
 *     <artifactId>localized-jpa-maven-plugin</artifactId>
 *     <version>0.1.0-SNAPSHOT</version>
 *     <executions>
 *         <execution>
 *             <goals>
 *                 <goal>configure</goal>
 *             </goals>
 *         </execution>
 *     </executions>
 * </plugin>
 * }</pre>
 * 
 * @since 0.1.0
 */
package com.localizedjpa.maven;
